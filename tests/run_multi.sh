cd ..
./run_server.sh &
sleep 0.5

for i in $(seq 1 $1)
do
	(echo "/name:t$i" | ./run_client.sh) &
	sleep 0.5
done

sleep 1

kill $(jobs -p) 2> /dev/null
