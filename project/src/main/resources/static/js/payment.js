document.getElementById('paymentForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const payerId = document.getElementById('payerId').value;
    const bookingId = document.getElementById('bookingId').value;
    const amount = document.getElementById('amount').value;
    const paymentMethod = document.getElementById('paymentMethod').value;
    const status = document.getElementById('status').value;

    const paymentData = {
        payer: { id: payerId },
        booking: { id: bookingId },
        amount: parseFloat(amount),
        paymentMethod: paymentMethod,
        status: status,
        paymentDate: new Date().toISOString() // Thời gian thanh toán
    };

    fetch('http://localhost:8080/payments', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(paymentData)
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Error in payment submission');
            }
        })
        .then(data => {
            document.getElementById('responseMessage').innerText = 'Payment successful! ID: ' + data.id;
        })
        .catch(error => {
            document.getElementById('responseMessage').innerText = 'Payment failed: ' + error.message;
        });
});