#!/bin/bash

# Script to set up git hooks for the project
# Run this after cloning the repository

echo "Setting up git hooks..."

# Configure git to use the .githooks directory
git config core.hooksPath .githooks

echo "✅ Git hooks configured successfully!"
echo "The pre-commit hook will now run tests before each commit."