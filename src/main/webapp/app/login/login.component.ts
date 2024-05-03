import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { AccountService } from '../auth/account.service';
import { LoginService } from '../login/login.service';
import { Account } from '../auth/account.model';

import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'jhi-login',
  templateUrl: './login.component.html',
  imports: [CommonModule, MatButtonModule, MatInputModule, MatCardModule, ReactiveFormsModule],
  standalone: true,
  styleUrl: './login.component.css',
})
export default class LoginComponent implements OnInit {
  private readonly destroy$ = new Subject<void>();

  account = signal<Account | null>(null);

  loginForm: FormGroup;

  private accountService = inject(AccountService);
  private loginService = inject(LoginService);
  private fb = inject(FormBuilder);

  constructor() {
    this.loginForm = this.fb.nonNullable.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
    });
    const destroyRef = inject(DestroyRef);
    destroyRef.onDestroy(() => {
      this.destroy$.next();
      this.destroy$.complete();
    });
  }

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => (this.account.set(account)));
  }

  login(): void {
    this.loginService
      .login({
        username: this.loginForm.value.username!,
        password: this.loginForm.value.password!,
      })
      .subscribe();
  }

  logout(): void {
    this.loginService.logout();
  }
}
