export interface User {
  id: number;
  name: string;
  email: string;
  passwordHash: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface ValidateRequest {
  success: boolean;
  message: string;
}

export interface Pixel {
  x: number;
  y: number;
  color: string;
  playerId: number;
  timestamp: string;
}

export interface GameField {
  id: number;
  pixels: Pixel[];
  width: number;
  height: number;
}

export interface Move {
  id?: number;
  gameId: number;
  playerId: number;
  x: number;
  y: number;
  color: string;
  timestamp: string;
} 