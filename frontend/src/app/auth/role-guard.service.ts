import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router} from '@angular/router';
import {AuthService} from './auth.service';
import decode from 'jwt-decode';

@Injectable()
export class RoleGuardService implements CanActivate {
  constructor(public auth: AuthService, public router: Router) {
  }

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const expectedRole = route.data.expectedRole;
    const token = localStorage.getItem('token');
    const tokenPayload = decode(token);
    if (
      !this.auth.isAuthenticated() ||
      tokenPayload.role !== expectedRole
    ) {
      this.router.navigate(['signin']);
      return false;
    }
    return true;
  }
}
