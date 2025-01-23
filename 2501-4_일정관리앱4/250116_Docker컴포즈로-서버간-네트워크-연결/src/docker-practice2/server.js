const express = require('express');
const redis = require("redis");

const PORT = 8080;

const redisClient = redis.createClient({
  socket: {
    host: "redis-server", //docker-compose.yml 파일에 명시한 컨테이너 이름으로 주면된다.
    port: 6379
}});
const app = express();

app.get('/', async (req, res) => {
  try {
    await redisClient.connect();

    let number = await redisClient.get('number');

    if (number == null) {
      number = 0;
    }

    console.log('Number: ' + number);
    res.send("숫자가 1씩 올라갑니다. 숫자: " + number);
    await redisClient.set("number", parseInt(number) + 1);
  } catch (error) {
    console.error('Error handling Redis operation:', error);
    res.status(500).send('Internal Server Error');
  } finally {
    await redisClient.disconnect();
  }
});

app.listen(PORT);
console.log(`Running on ${PORT}`);