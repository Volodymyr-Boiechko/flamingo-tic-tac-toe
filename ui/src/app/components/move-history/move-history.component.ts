import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { Move } from '../../models/session.model';

@Component({
  selector: 'app-move-history',
  standalone: true,
  imports: [MatListModule, MatIconModule, DatePipe],
  templateUrl: './move-history.component.html',
  styleUrl: './move-history.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MoveHistoryComponent {
  readonly moves = input.required<Move[]>();
}
