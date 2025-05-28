import React, { useState } from 'react';
import {
  Box,
  Button,
  FormControl,
  FormLabel,
  Input,
  VStack,
  useToast,
  Heading,
  Text,
  Center,
} from '@chakra-ui/react';
import { registerUser } from '../../api';
import { useNavigate } from 'react-router-dom';
import type { ValidateRequest } from '../../types';

export const RegisterForm: React.FC = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const toast = useToast();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      const user = await registerUser(username, email, password);
      if (user.id) {
        localStorage.setItem('userId', user.id.toString());
        localStorage.setItem('username', user.name);
        toast({
          title: 'Registration successful',
          description: 'Welcome to Pixel Battle!',
          status: 'success',
          duration: 3000,
        });
        navigate('/game');
      }
    } catch (error: any) {
      const response = error.response?.data as ValidateRequest;
      const message = response?.message || 'Please try again';
      toast({
        title: 'Registration failed',
        description: message,
        status: 'error',
        duration: 3000,
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Center minH="80vh">
      <Box 
        maxW="md" 
        w="full" 
        p={8} 
        borderWidth={1} 
        borderRadius="lg" 
        boxShadow="lg"
        bg="white"
      >
        <VStack spacing={6} align="stretch">
          <Heading textAlign="center" size="xl" mb={2}>Join Pixel Battle</Heading>
          <Text textAlign="center" color="gray.600" fontSize="lg">
            Create your account to start placing pixels
          </Text>
          <form onSubmit={handleSubmit}>
            <VStack spacing={4}>
              <FormControl isRequired>
                <FormLabel>Username</FormLabel>
                <Input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter your username"
                  minLength={3}
                  maxLength={50}
                  size="lg"
                />
              </FormControl>
              <FormControl isRequired>
                <FormLabel>Email</FormLabel>
                <Input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Enter your email"
                  size="lg"
                />
              </FormControl>
              <FormControl isRequired>
                <FormLabel>Password</FormLabel>
                <Input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  minLength={6}
                  size="lg"
                />
              </FormControl>
              <Button
                type="submit"
                colorScheme="blue"
                width="full"
                size="lg"
                isLoading={isLoading}
                loadingText="Registering..."
                mt={4}
              >
                Register
              </Button>
            </VStack>
          </form>
        </VStack>
      </Box>
    </Center>
  );
}; 