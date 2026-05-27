export type PlayerValue = 'X' | 'O';

export type GameStatusValue = 'IN_PROGRESS' | 'X_WON' | 'O_WON' | 'DRAW';

export type SessionStatus = 'CREATED' | 'SIMULATING' | 'FINISHED' | 'FAILED';

export interface CreateSessionResponse {
  sessionId: string;
  gameId: string;
  createdAt: string;
}

export interface SessionDetailsResponse {
  sessionId: string;
  gameId: string;
  status: SessionStatus;
  moves: Move[];
  createdAt: string;
  finishedAt: string | null;
}

export interface Move {
  player: PlayerValue;
  row: number;
  col: number;
  timestamp: string;
}

export type Cell = PlayerValue | null;

export type Board = Cell[][];

export function emptyBoard(): Board {
  return [
    [null, null, null],
    [null, null, null],
    [null, null, null],
  ];
}
