import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {PageNotFoundComponent} from "./page-not-found/page-not-found.component";
import {SigninComponent} from "./public/signin/signin.component";
import {SignupComponent} from "./public/signup/signup.component";
import {AccountComponent} from "./account/account.component";
import {AppRoutingModule} from "./app-routing.module";
import {HttpClientModule} from "@angular/common/http";
import {AuthGuardService} from "./auth/auth-guard.service";
import {RoleGuardService} from "./auth/role-guard.service";
import {AuthService} from "./auth/auth.service";
import {JwtHelperService, JwtModule} from "@auth0/angular-jwt";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CookieService} from "ngx-cookie-service";

export function tokenGetter() {
  return localStorage.getItem("access_token");
}

@NgModule({
  declarations: [
    AppComponent,
    PageNotFoundComponent,
    SigninComponent,
    SignupComponent,
    AccountComponent
  ],
  imports: [
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserModule,
    HttpClientModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: tokenGetter,
        whitelistedDomains: ["localhost:8080"]
      }
    })
  ],
  providers: [
    CookieService,
    AuthGuardService,
    RoleGuardService,
    AuthService,
    JwtHelperService,
    FormBuilder
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
