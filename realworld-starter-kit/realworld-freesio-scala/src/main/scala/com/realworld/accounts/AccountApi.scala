package com.realworld.accounts

import cats.effect.Effect
import cats.implicits._
import com.realworld.AppError
import com.realworld.accounts.model.{AccountEntity, AccountForm}
import com.realworld.accounts.services.AccountServices
import com.realworld.app.errorhandler.HttpErrorHandler
import freestyle.tagless.logging.LoggingM
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl



class AccountApi[F[_]: Effect](implicit services: AccountServices[F], log: LoggingM[F], H: HttpErrorHandler[F, AppError]) extends Http4sDsl[F] {
  private val prefix = "users"

  import Codecs._

  val endPoints = HttpRoutes.of[F] {
    case POST -> Root / prefix =>
      for {
        _        <- log.debug("POST /users reset")
        reset <- services.reset
        res <- Ok(reset.asJson)
      } yield res

    case req@POST -> Root / prefix / "register" =>
      (for {
          account <- req.as[AccountEntity]
          insertedAccount <- services.registerUser(account)
          response <- insertedAccount.fold(NotFound(s"Could not register ${services.model} with ${account.email}"))(account => Ok(account.asJson))
        } yield response)

    case GET -> Root / prefix / (email) =>
      services.getCurrentUser(email) flatMap { item =>
        item.fold(NotFound(s"Could not find ${services.model} with $email"))(account => Ok(account.asJson))
    }

    case DELETE -> Root / prefix / LongVar(id) =>
      services.deleteUser(id) flatMap { item => Ok(item.asJson)}

    case req@PUT -> Root / prefix  =>
      for {
        account <- req.as[AccountForm]
        updatedAccount <- services.updateUser(account)
        response <- updatedAccount.fold(NotFound(s"Could not register ${services.model} with ${account.email}"))(account => Ok(account.asJson))
      } yield response

    case req@PUT -> Root / prefix / "password" =>
      for {
        account <- req.as[AccountForm]
        updatedAccount <- services.updatePassword(account)
        response <- updatedAccount.fold(NotFound(s"Could not register ${services.model} with ${account.email}"))(account => Ok(account.asJson))
      } yield response

    case GET -> Root / prefix / "hello" =>
      for {
        _        <- log.error("Not really an error")
        _        <- log.warn("Not really a warn")
        _        <- log.debug("GET /Hello")
        response <- Ok("Hello World")
      } yield response
  }

}


object AccountApi {
  implicit def instance[F[_]: Effect](implicit services: AccountServices[F], log: LoggingM[F], H: HttpErrorHandler[F, AppError]): AccountApi[F] = new AccountApi[F]
}
