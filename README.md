# car-booking-service

 A  Spring Boot microservice that manages car rental bookings: confirms
bookings based on payment mode, confirms bank-transfer payments received via Kafka, and
auto-cancells unpaid bank-transfer bookings close to the rental start date via Scheduler.

## Running it

```
./mvnw test
```


Build a container image:
```
docker build -t car-booking-service .
docker run -p 8080:8080 car-booking-service
```

If you want to bring up the app together with its dependencies, try the below
```
docker compose up -d
```

## API

### `POST /api/v1/bookings/confirm`

```json
{
  "customerName": "ANJALI",
  "vehicleId": "NL123454",
  "rentalStartDate": "2026-10-20",
  "rentalEndDate": "2026-10-29",
  "vehicleCategory": "Luxury",
  "paymentMode": "BANK_TRANSFER",
  "paymentReference": "TXN987658325"
}
```
Response (`201 Created`):

```json
{
  "bookingId": "BKG0000001",
  "status": "PENDING_PAYMENT"
}
```
```vehicleCategory - Compact, Sedan, SUV, Luxury```

```paymentMode - CASH, DIGITAL_WALLET, CREDIT_CARD, BANK_TRANSFER```

```bookingStatus- PENDING_PAYMENT, CONFIRMED, CANCELLED```

paymentReference is required when paymentMode is CREDIT_CARD

### `GET /api/v1/bookings/{bookingId}`

Response (`201 Created`):

```json
{
  "bookingId": "BKG0012345",
  "status": "PENDING_PAYMENT"
}
```
