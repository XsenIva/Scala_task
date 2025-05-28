import React from 'react';
import { ChakraProvider, Box } from '@chakra-ui/react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { RegisterForm } from './components/Auth/RegisterForm';
import { GameBoard } from './components/Game/GameBoard';
import theme from './theme';

const App: React.FC = () => {
  return (
    <ChakraProvider theme={theme}>
      <Router>
        <Box p={4}>
          <Routes>
            <Route path="/register" element={<RegisterForm />} />
            <Route path="/game" element={<GameBoard />} />
            <Route path="/" element={<Navigate to="/register" replace />} />
          </Routes>
        </Box>
      </Router>
    </ChakraProvider>
  );
};

export default App;
