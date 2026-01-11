#!/bin/bash

# Load test script - runs the sequence 500 times
# Sequence: create lobby -> add dummy players -> start game -> sporadically check game state

SERVER="http://localhost:8080"
ITERATIONS=500

declare -a LOBBY_IDS
declare -a PLAYER_IDS

echo "Starting load test with $ITERATIONS iterations..."
echo "Target server: $SERVER"
echo "-------------------------------------------"

for i in $(seq 1 $ITERATIONS); do
  echo "Iteration $i/$ITERATIONS"

  CREATE_RESPONSE=$(curl -s "$SERVER/lobby/create" \
    -H "Content-Type: application/json" \
    --data-raw '{"playerName":"Manohar"}')

  LOBBY_ID=$(echo "$CREATE_RESPONSE" | grep -o '"lobbyId":"[^"]*"' | cut -d'"' -f4)
  PLAYER_ID=$(echo "$CREATE_RESPONSE" | grep -o '"playerId":"[^"]*"' | cut -d'"' -f4)

  if [ -z "$LOBBY_ID" ] || [ -z "$PLAYER_ID" ]; then
    echo "  ERROR: Failed to extract lobbyId or playerId from response: $CREATE_RESPONSE"
    continue
  fi

  LOBBY_IDS+=($LOBBY_ID)
  PLAYER_IDS+=($PLAYER_ID)

  echo "  Created lobby: $LOBBY_ID, player: $PLAYER_ID"

  curl -s "$SERVER/adddummyplayers?lobbyId=$LOBBY_ID&size=15" >/dev/null
  echo "  Added dummy players to lobby: $LOBBY_ID"

  curl -s "$SERVER/game/start" \
    -H "Content-Type: application/json" \
    --data-raw "{\"lobbyId\":\"$LOBBY_ID\",\"playerId\":\"$PLAYER_ID\"}" >/dev/null
  echo "  Started game for lobby: $LOBBY_ID"

  if [ $((i % 10)) -eq 0 ] && [ ${#LOBBY_IDS[@]} -gt 0 ]; then
    echo "  Checking game states for existing lobbies..."

    for j in $(seq 1 5); do
      if [ ${#LOBBY_IDS[@]} -gt 0 ]; then
        RANDOM_INDEX=$((RANDOM % ${#LOBBY_IDS[@]}))
        STATE_RESPONSE=$(curl -s "$SERVER/game/state" \
          -H "Content-Type: application/json" \
          -H "Accept: application/json, text/plain, */*" \
          --data-raw "{\"lobbyId\":\"${LOBBY_IDS[$RANDOM_INDEX]}\",\"playerId\":\"${PLAYER_IDS[$RANDOM_INDEX]}\"}")
        echo "    State check for lobby ${LOBBY_IDS[$RANDOM_INDEX]}: ${STATE_RESPONSE:0:50}..."
      fi
    done
  fi

  echo "  ✓ Completed iteration $i"
  echo ""
done

echo "-------------------------------------------"
echo "Load test completed: $ITERATIONS iterations"
echo "Total lobbies created: ${#LOBBY_IDS[@]}"
echo ""
echo "Running final game state checks for all lobbies..."
for idx in "${!LOBBY_IDS[@]}"; do
  curl -s "$SERVER/game/state" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json, text/plain, */*" \
    --data-raw "{\"lobbyId\":\"${LOBBY_IDS[$idx]}\",\"playerId\":\"${PLAYER_IDS[$idx]}\"}" >/dev/null
  if [ $((idx % 50)) -eq 0 ]; then
    echo "  Checked $idx lobbies..."
  fi
done
echo "Final state checks completed for all ${#LOBBY_IDS[@]} lobbies"
echo ""
echo "-------------------------------------------"
echo "Starting continuous load testing (infinite loop)..."
echo "Press Ctrl+C to stop"
echo "-------------------------------------------"

COUNTER=0
while true; do
  COUNTER=$((COUNTER + 1))

  NUM_CHECKS=$((5000 + RANDOM % 6))

  echo "[$(date '+%Y-%m-%d %H:%M:%S')] Batch $COUNTER: Checking $NUM_CHECKS random lobbies..."

  for j in $(seq 1 $NUM_CHECKS); do
    if [ ${#LOBBY_IDS[@]} -gt 0 ]; then
      RANDOM_INDEX=$((RANDOM % ${#LOBBY_IDS[@]}))

      STATE_RESPONSE=$(curl -s "$SERVER/game/state" \
        -H "Content-Type: application/json" \
        -H "Accept: application/json, text/plain, */*" \
        --data-raw "{\"lobbyId\":\"${LOBBY_IDS[$RANDOM_INDEX]}\",\"playerId\":\"${PLAYER_IDS[$RANDOM_INDEX]}\"}")
      echo $j

    fi
  done

  SLEEP_TIME=$(awk -v min=0.5 -v max=1 'BEGIN{srand(); print min+rand()*(max-min)}')
  sleep $SLEEP_TIME
done
