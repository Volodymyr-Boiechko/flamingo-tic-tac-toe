import { ChangeDetectionStrategy, Component, input } from '@angular/core';
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
}
