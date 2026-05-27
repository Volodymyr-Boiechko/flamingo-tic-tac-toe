import { GameStatusValue, Move } from './session.model';

export type SimulationEventType = 'MOVE_MADE' | 'GAME_FINISHED' | 'ERROR';

export interface SimulationEvent {
  type: SimulationEventType;
  move: Move | null;
  status: GameStatusValue | null;
  message: string | null;
}
