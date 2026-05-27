import { ChangeDetectionStrategy, Component, effect, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { GameBoardComponent } from './components/game-board/game-board.component';
import { GameStatusComponent } from './components/game-status/game-status.component';
import { MoveHistoryComponent } from './components/move-history/move-history.component';
import { StartControlsComponent } from './components/start-controls/start-controls.component';
import { GameEventsService } from './services/game-events.service';
import { SessionService } from './services/session.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    MatCardModule,
    MatIconModule,
    GameBoardComponent,
    GameStatusComponent,
    MoveHistoryComponent,
    StartControlsComponent,
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent {
  private readonly sessionService = inject(SessionService);
  private readonly snackBar = inject(MatSnackBar);

  readonly events = inject(GameEventsService);
  readonly isLoading = signal(false);

  constructor() {
    effect(() => {
      const err = this.events.error();
      if (err) {
        this.snackBar.open(err, 'Dismiss', { duration: 5000 });
      }
    });
  }

  start(): void {
    this.isLoading.set(true);
    this.events.reset();

    this.sessionService.createSession().subscribe({
      next: (session) => {
        this.sessionService.startSimulation(session.sessionId).subscribe({
          next: () => {
            this.events.connect(session.sessionId);
            this.isLoading.set(false);
          },
          error: () => {
            this.snackBar.open('Failed to start simulation', 'Dismiss', { duration: 5000 });
            this.isLoading.set(false);
          },
        });
      },
      error: () => {
        this.snackBar.open('Failed to create session', 'Dismiss', { duration: 5000 });
        this.isLoading.set(false);
      },
    });
  }

  reset(): void {
    this.events.disconnect();
    this.events.reset();
  }
}
