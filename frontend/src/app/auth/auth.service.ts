import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";

interface AccessToken {
  access_token: string;
}

@Injectable()
export class AuthService {

  constructor(private http: HttpClient) {
  }

  signin(login: any) {
    return this.http.post<AccessToken>('/backend/signin', login, /*{ withCredentials: true }*/);
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('xsrfToken');
    return token !== undefined;
  }

  protected() {
    const token = localStorage.getItem('xsrfToken');
    const headers = new HttpHeaders({'x-xsrf-token':token});
    return this.http.get('/backend/protected', { headers, withCredentials: true });
  }

}
