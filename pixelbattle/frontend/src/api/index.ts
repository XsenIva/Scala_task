import axios from 'axios';
import type { GameField, Move, User, RegisterRequest, ValidateRequest } from '../types';

const API_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const registerUser = async (username: string, email: string, password: string): Promise<User> => {
  const response = await api.post<User>('/players/register', {
    username,
    email,
    password
  });
  return response.data;
};

export const loginUser = async (email: string, password: string): Promise<User> => {
  const response = await api.post<User>('/players/login', {
    email,
    password
  });
  return response.data;
};

export const getCurrentGame = async (): Promise<GameField> => {
  const response = await api.get('/game');
  return response.data;
};

export const createNewGame = async (): Promise<GameField> => {
  const response = await api.post('/game');
  return response.data;
};

export const placePixel = async (x: number, y: number, color: string, playerId: number): Promise<Move> => {
  const response = await api.post('/game/pixel', null, {
    params: { x, y, color, player_id: playerId }
  });
  return response.data;
}; 