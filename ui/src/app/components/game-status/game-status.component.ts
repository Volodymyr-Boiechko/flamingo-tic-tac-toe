import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { GameStatusValue, PlayerValue } from '../../models/session.model';

@Component({
  selector: 'app-game-status',
  standalone: true,
  imports: [MatChipsModule, MatIconModule],
  templateUrl: './game-status.component.html',
  styleUrl: './game-status.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameStatusComponent {
  readonly status = input.required<GameStatusValue>();
  readonly winner = input<PlayerValue | null>(null);
  readonly moveCount = input<number>(0);

  readonly icon = computed(() => {
    switch (this.status()) {
      case 'IN_PROGRESS':
        return 'sports_esports';
      case 'X_WON':
      case 'O_WON':
        return 'emoji_events';
      case 'DRAW':
        return 'handshake';
    }
  });

  readonly inProgressLabel = computed(() => {
    return this.moveCount() === 0 ? 'Ready to start' : 'In progress';
  });
}
