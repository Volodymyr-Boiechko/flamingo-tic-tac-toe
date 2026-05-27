import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-start-controls',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './start-controls.component.html',
  styleUrl: './start-controls.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StartControlsComponent {
  readonly isRunning = input<boolean>(false);
  readonly isFinished = input<boolean>(false);
  readonly isLoading = input<boolean>(false);

  readonly start = output<void>();
  readonly reset = output<void>();
}
