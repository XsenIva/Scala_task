import React from 'react';
import { ChakraProvider, Box, Container, Center } from '@chakra-ui/react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { RegisterForm } from './components/Auth/RegisterForm';
import { GameBoard } from './components/Game/GameBoard';
import theme from './theme';

const App: React.FC = () => {
  return (
    <ChakraProvider theme={theme}>
      <Router>
        <Box 
          minH="100vh" 
          bg="gray.50" 
          display="flex" 
          alignItems="center" 
          justifyContent="center"
        >
          <Container 
            maxW="container.xl" 
            py={8} 
            display="flex" 
            flexDirection="column" 
            alignItems="center" 
            justifyContent="center" 
            flex="1"
          >
            <Routes>
              <Route path="/register" element={<RegisterForm />} />
              <Route path="/game" element={<GameBoard />} />
              <Route path="/" element={<Navigate to="/register" replace />} />
            </Routes>
          </Container>
        </Box>
      </Router>
    </ChakraProvider>
  );
};

export default App;
