package com.realworld.app

import com.realworld.accounts.services.{AccountServices, AuthServices}
import com.realworld.app.services.AppServices
import com.realworld.articles.services.ArticlesServices
import com.realworld.profile.ProfileServices
import freestyle.tagless.module

@module
trait Services[F[_]] {
  val accountServices: AccountServices[F]
  val authServices: AuthServices[F]
  val profileServices: ProfileServices[F]
  val appServices: AppServices[F]
  val articlesServices: ArticlesServices[F]
}
