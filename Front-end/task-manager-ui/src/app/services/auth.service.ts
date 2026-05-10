import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'currentUser';
  private isBrowser: boolean;

  constructor(
    @Inject(PLATFORM_ID) platformId: object,
    private router: Router
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  // ── Token Management ──────────────────────────────────────────

  saveToken(token: string): void {
    if (this.isBrowser) {
      localStorage.setItem(this.TOKEN_KEY, token);
    }
  }

  getToken(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem(this.TOKEN_KEY);
  }

  removeToken(): void {
    if (this.isBrowser) {
      localStorage.removeItem(this.TOKEN_KEY);
    }
  }

  // ── User Management ───────────────────────────────────────────

  saveUser(user: any): void {
    if (this.isBrowser) {
      // Strip token before storing user profile (stored separately)
      const { token, ...userWithoutToken } = user;
      localStorage.setItem(this.USER_KEY, JSON.stringify(userWithoutToken));
    }
  }

  getUser(): any | null {
    if (!this.isBrowser) return null;
    const raw = localStorage.getItem(this.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  removeUser(): void {
    if (this.isBrowser) {
      localStorage.removeItem(this.USER_KEY);
    }
  }

  // ── Session ───────────────────────────────────────────────────

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    return !this.isTokenExpired(token);
  }

  logout(): void {
    this.removeToken();
    this.removeUser();
  }

  // ── JWT Helpers ───────────────────────────────────────────────

  private decodeToken(token: string): any | null {
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded);
    } catch {
      return null;
    }
  }

  isTokenExpired(token: string): boolean {
    const decoded = this.decodeToken(token);
    if (!decoded || !decoded.exp) return true;
    const expiryMs = decoded.exp * 1000;
    return Date.now() > expiryMs;
  }

  getTokenExpiry(): Date | null {
    const token = this.getToken();
    if (!token) return null;
    const decoded = this.decodeToken(token);
    if (!decoded?.exp) return null;
    return new Date(decoded.exp * 1000);
  }

  getRoleFromToken(): string | null {
    const token = this.getToken();
    if (!token) return null;
    const decoded = this.decodeToken(token);
    return decoded?.role ?? null;
  }
}
