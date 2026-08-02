import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Public auth endpoints do not need a token.
  if (
    req.url.includes('/users/login') ||
    req.url.includes('/users/company-signup') ||
    req.url.includes('/users/forgot-password')
  ) {
    return next(req);
  }

  // Clone request and attach Authorization header if token exists
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 401 = token expired or invalid → force logout
      if (error.status === 401) {
        authService.logout();
        // Reload to show login page (app.ts checks currentUser)
        if (typeof window !== 'undefined') {
          window.location.reload();
        }
      }
      return throwError(() => error);
    })
  );
};
