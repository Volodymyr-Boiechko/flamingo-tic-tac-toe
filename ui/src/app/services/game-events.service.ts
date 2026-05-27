import { computed, Injectable, signal } from '@angular/core';
import { environment } from '../../environments/environment';
import { Board, emptyBoard, GameStatusValue, Move, PlayerValue } from '../models/session.model';
import { SimulationEvent } from '../models/simulation-event.model';

@Injectable({ providedIn: 'root' })
export class GameEventsService {
  private readonly _board = signal<Board>(emptyBoard());
  private readonly _status = signal<GameStatusValue>('IN_PROGRESS');
  private readonly _moves = signal<Move[]>([]);
  private readonly _isConnected = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly board = this._board.asReadonly();
  readonly status = this._status.asReadonly();
  readonly moves = this._moves.asReadonly();
  readonly isConnected = this._isConnected.asReadonly();
  readonly error = this._error.asReadonly();

  readonly isFinished = computed(() => this._status() !== 'IN_PROGRESS');
  readonly winner = computed<PlayerValue | null>(() => {
    const s = this._status();
    if (s === 'X_WON') return 'X';
    if (s === 'O_WON') return 'O';
    return null;
  });
  readonly moveCount = computed(() => this._moves().length);

  private eventSource: EventSource | null = null;

  connect(sessionId: string): void {
    this.disconnect();
    this.reset();

    const url = `${environment.sessionApiUrl}/sessions/${sessionId}/events`;
    this.eventSource = new EventSource(url);
    this._isConnected.set(true);

    this.eventSource.onmessage = (e) => {
      try {
        const event: SimulationEvent = JSON.parse(e.data);
        this.handleEvent(event);
      } catch (err) {
        this._error.set('Failed to parse simulation event');
      }
    };

    this.eventSource.onerror = () => {
      if (!this.isFinished()) {
        this._error.set('Connection to simulation lost');
      }
      this.disconnect();
    };
  }

  disconnect(): void {
    this.eventSource?.close();
    this.eventSource = null;
    this._isConnected.set(false);
  }

  reset(): void {
    this._board.set(emptyBoard());
    this._status.set('IN_PROGRESS');
    this._moves.set([]);
    this._error.set(null);
  }

  private handleEvent(event: SimulationEvent): void {
    switch (event.type) {
      case 'MOVE_MADE':
        if (event.move) {
          this.applyMove(event.move);
        }
        break;
      case 'GAME_FINISHED':
        if (event.status) {
          this._status.set(event.status);
        }
        break;
      case 'ERROR':
        this._error.set(event.message ?? 'Unknown simulation error');
        break;
    }
  }

  private applyMove(move: Move): void {
    this._board.update((board) => {
      const newBoard = board.map((row) => [...row]);
      newBoard[move.row][move.col] = move.player;
      return newBoard;
    });
    this._moves.update((moves) => [...moves, move]);
  }
}
