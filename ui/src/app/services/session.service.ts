import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CreateSessionResponse, SessionDetailsResponse } from '../models/session.model';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.sessionApiUrl;

  createSession(): Observable<CreateSessionResponse> {
    return this.http.post<CreateSessionResponse>(`${this.baseUrl}/sessions`, {});
  }

  startSimulation(sessionId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/sessions/${sessionId}/simulate`, {});
  }

  getSession(sessionId: string): Observable<SessionDetailsResponse> {
    return this.http.get<SessionDetailsResponse>(`${this.baseUrl}/sessions/${sessionId}`);
  }
}
