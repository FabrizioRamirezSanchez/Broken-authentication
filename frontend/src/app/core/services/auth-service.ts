import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth';

  login(credentials: {username: string; password: string}): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials);
  }

  resetPassword(data: any): Observable<string> {
    return this.http.post(
      'http://localhost:8080/api/auth/reset-password',
      data,
      { responseType: 'text' }
    );
  }

  register(data: {username: string; email: string; password: string; role: string}): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, data);
  }

  verifyToken(token: string): Observable<any> {
    const params = new HttpParams().set('token', token);
    return this.http.get(`${this.apiUrl}/verify-token?token=`, {params});
  }
}
