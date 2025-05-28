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
    <Box maxW="md" mx="auto" mt={8}>
      <VStack spacing={6} align="stretch">
        <Heading textAlign="center">Join Pixel Battle</Heading>
        <Text textAlign="center" color="gray.600">
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
              />
            </FormControl>
            <FormControl isRequired>
              <FormLabel>Email</FormLabel>
              <Input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
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
              />
            </FormControl>
            <Button
              type="submit"
              colorScheme="blue"
              width="full"
              isLoading={isLoading}
              loadingText="Registering..."
            >
              Register
            </Button>
          </VStack>
        </form>
      </VStack>
    </Box>
  );
}; 