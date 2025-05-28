import React, { useEffect, useState, useRef } from 'react';
import {
  Box,
  Button,
  HStack,
  useToast,
  Center,
  Spinner,
  Text,
  Heading,
  VStack,
} from '@chakra-ui/react';
import { getCurrentGame, placePixel } from '../../api';
import type { GameField, Pixel } from '../../types';

const CELL_SIZE = 25;
const GRID_COLOR = '#CCCCCC';
const GRID_WIDTH = 1;
const COLORS = ['#FF0000', '#00FF00', '#0000FF', '#FFFF00', '#FF00FF', '#00FFFF', '#000000', '#FFFFFF'];

export const GameBoard: React.FC = () => {
  const [gameField, setGameField] = useState<GameField | null>(null);
  const [selectedColor, setSelectedColor] = useState(COLORS[0]);
  const [isLoading, setIsLoading] = useState(false);
  const [hoveredCell, setHoveredCell] = useState<{ x: number; y: number } | null>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const toast = useToast();

  const drawPixel = (ctx: CanvasRenderingContext2D, x: number, y: number, color: string, isHovered: boolean = false) => {
    const actualX = x * CELL_SIZE;
    const actualY = y * CELL_SIZE;
    
    // Заливка клетки
    ctx.fillStyle = color;
    ctx.fillRect(actualX, actualY, CELL_SIZE, CELL_SIZE);
    
    // Рамка клетки
    ctx.strokeStyle = isHovered ? '#000000' : GRID_COLOR;
    ctx.lineWidth = isHovered ? 2 : GRID_WIDTH;
    ctx.strokeRect(actualX, actualY, CELL_SIZE, CELL_SIZE);
  };

  const drawGrid = (ctx: CanvasRenderingContext2D, width: number, height: number) => {
    ctx.strokeStyle = GRID_COLOR;
    ctx.lineWidth = GRID_WIDTH;

    // Горизонтальные линии
    for (let y = 0; y <= height; y++) {
      ctx.beginPath();
      ctx.moveTo(0, y * CELL_SIZE);
      ctx.lineTo(width * CELL_SIZE, y * CELL_SIZE);
      ctx.stroke();
    }

    // Вертикальные линии
    for (let x = 0; x <= width; x++) {
      ctx.beginPath();
      ctx.moveTo(x * CELL_SIZE, 0);
      ctx.lineTo(x * CELL_SIZE, height * CELL_SIZE);
      ctx.stroke();
    }
  };

  const drawBoard = () => {
    const canvas = canvasRef.current;
    if (!canvas || !gameField) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) {
      console.error('Could not get 2D context from canvas');
      return;
    }

    // Устанавливаем размеры canvas
    canvas.width = gameField.width * CELL_SIZE;
    canvas.height = gameField.height * CELL_SIZE;

    // Очищаем canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Заполняем все клетки белым цветом по умолчанию
    for (let x = 0; x < gameField.width; x++) {
      for (let y = 0; y < gameField.height; y++) {
        drawPixel(ctx, x, y, '#FFFFFF');
      }
    }
    
    // Рисуем пиксели
    gameField.pixels.forEach((pixel: Pixel) => {
      drawPixel(ctx, pixel.x, pixel.y, pixel.color);
    });

    // Рисуем подсвеченную клетку
    if (hoveredCell) {
      drawPixel(ctx, hoveredCell.x, hoveredCell.y, selectedColor, true);
    }
  };

  const handleCanvasClick = async (event: React.MouseEvent<HTMLCanvasElement>) => {
    if (!gameField || isLoading) return;

    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const x = Math.floor((event.clientX - rect.left) / CELL_SIZE);
    const y = Math.floor((event.clientY - rect.top) / CELL_SIZE);

    if (x < 0 || x >= gameField.width || y < 0 || y >= gameField.height) return;

    const userId = localStorage.getItem('userId');
    if (!userId) {
      toast({
        title: 'Please login first',
        status: 'error',
        duration: 3000,
      });
      return;
    }

    setIsLoading(true);
    try {
      await placePixel(x, y, selectedColor, parseInt(userId));
      const updatedGame = await getCurrentGame();
      setGameField(updatedGame);
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to place pixel';
      toast({
        title: 'Error',
        description: message,
        status: 'error',
        duration: 3000,
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCanvasMouseMove = (event: React.MouseEvent<HTMLCanvasElement>) => {
    if (!gameField || isLoading) return;

    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const x = Math.floor((event.clientX - rect.left) / CELL_SIZE);
    const y = Math.floor((event.clientY - rect.top) / CELL_SIZE);

    if (x < 0 || x >= gameField.width || y < 0 || y >= gameField.height) {
      setHoveredCell(null);
      return;
    }

    setHoveredCell({ x, y });
  };

  const handleCanvasMouseLeave = () => {
    setHoveredCell(null);
  };

  useEffect(() => {
    const fetchGame = async () => {
      try {
        const game = await getCurrentGame();
        setGameField(game);
      } catch (error: any) {
        const message = error.response?.data?.message || 'Failed to load game';
        toast({
          title: 'Error',
          description: message,
          status: 'error',
          duration: 3000,
        });
      }
    };

    fetchGame();
    const interval = setInterval(fetchGame, 5000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (gameField) {
      requestAnimationFrame(drawBoard);
    }
  }, [gameField, hoveredCell, selectedColor]);

  if (!gameField) {
    return (
      <Center h="100vh">
        <Spinner size="xl" />
      </Center>
    );
  }

  return (
    <Box 
      w="full" 
      display="flex" 
      flexDirection="column" 
      alignItems="center" 
      justifyContent="center"
      minH="full"
    >
      <VStack spacing={8} align="center" w="full">
        <Heading as="h1" size="2xl">
        ⚔️ Pixel Battle 🛡️
        </Heading>

        <Box>
          <Text mb={4} fontSize="xl" fontWeight="medium" textAlign="center">
            Тыкни цвет:
          </Text>
          <HStack spacing={3} justify="center">
            {COLORS.map((color) => (
              <Button
                key={color}
                bg={color}
                w="50px"
                h="50px"
                onClick={() => setSelectedColor(color)}
                border={color === selectedColor ? '3px solid black' : '1px solid gray.200'}
                _hover={{ 
                  opacity: 0.8,
                  transform: 'scale(1.1)',
                }}
                transition="all 0.2s"
                borderRadius="md"
              />
            ))}
          </HStack>
        </Box>

        <Box
          position="relative"
          borderWidth={2}
          borderColor="gray.200"
          borderRadius="xl"
          overflow="hidden"
          boxShadow="2xl"
          bg="white"
          p={4}
        >
          <canvas
            ref={canvasRef}
            style={{ 
              display: 'block',
            }}
            onClick={handleCanvasClick}
            onMouseMove={handleCanvasMouseMove}
            onMouseLeave={handleCanvasMouseLeave}
          />
        </Box>
      </VStack>
    </Box>
  );
}; 