import {NgModule} from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {PageNotFoundComponent} from "./page-not-found/page-not-found.component";
import {SigninComponent} from "./public/signin/signin.component";
import {SignupComponent} from "./public/signup/signup.component";
import {AccountComponent} from "./account/account.component";
import {AuthGuardService} from "./auth/auth-guard.service";
import {RoleGuardService} from "./auth/role-guard.service";


const appRoutes: Routes = [
  { path: 'signin', component: SigninComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'account', component: AccountComponent, canActivate: [ AuthGuardService ] },
  { path: 'accounting', component: PageNotFoundComponent, canActivate: [ RoleGuardService ], data: { expectedRole: 'admin' }  },
  { path: '', redirectTo: '/signin', pathMatch: 'full' },
  { path: '**', component: PageNotFoundComponent }
];


@NgModule({
  imports: [
    RouterModule.forRoot(
      appRoutes
    )
  ],
  exports: [
    RouterModule
  ]
})
export class AppRoutingModule {}
