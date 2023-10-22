cd ..
./run_server.sh > tests/log/server &
sleep 1

for i in $(seq 1 $1)
do
	mkfifo "tests/fifo$i"
	((cat < "tests/fifo$i" | ./run_client.sh) > tests/log/t$i) &
	echo "/name:t$i" > "tests/fifo$i"
	sleep 0.2
done

for i in $(seq 1 $2)
do
	# Random string gen from https://unix.stackexchange.com/questions/230673/how-to-generate-a-random-string
	message=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 50)
	sender=$((1 + $RANDOM % $1))
	reciever=$((1 + $RANDOM % $1))

	echo "/whisper:t${reciever};;${message}" > "tests/fifo${sender}"

	sleep 0.2
done

rm tests/fifo*
