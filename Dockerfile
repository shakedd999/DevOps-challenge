# Use the official Node.js image as the base
FROM node:21-alpine

# Set the working directory in the container
WORKDIR /app

# Copy only the package.json and package-lock.json (if available) first
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application code
COPY . .

# Expose the port your app runs on
EXPOSE 8080

# Set the command to run your app
CMD ["node", "app.js"]