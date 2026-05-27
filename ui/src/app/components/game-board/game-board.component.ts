import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Board } from '../../models/session.model';

@Component({
  selector: 'app-game-board',
  standalone: true,
  imports: [],
  templateUrl: './game-board.component.html',
  styleUrl: './game-board.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameBoardComponent {
  readonly board = input.required<Board>();
  readonly winningCells = input<Array<[number, number]>>([]);

  readonly winningSet = computed(() => {
    return new Set(this.winningCells().map(([r, c]) => `${r},${c}`));
  });

  isWinning(row: number, col: number): boolean {
    return this.winningSet().has(`${row},${col}`);
  }
}
