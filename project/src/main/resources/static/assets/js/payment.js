const paymentForm = document.getElementById('paymentForm');
const paymentList = document.getElementById('paymentList');

paymentForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const amount = document.getElementById('amount').value;
    const description = document.getElementById('description').value;

    const payment = { amount, description };

    const response = await fetch('http://localhost:8090/payments', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payment)
    });

    if (response.ok) {
        const savedPayment = await response.json();
        addPaymentToList(savedPayment);
        paymentForm.reset();
    } else {
        alert('Error creating payment');
    }
});

async function fetchPayments() {
    const response = await fetch('http://localhost:8090/payments');
    const payments = await response.json();
    payments.forEach(payment => addPaymentToList(payment));
}

function addPaymentToList(payment) {
    const li = document.createElement('li');
    li.textContent = `Amount: ${payment.amount}, Description: ${payment.description}`;

    const deleteButton = document.createElement('button');
    deleteButton.textContent = 'Delete';
    deleteButton.onclick = async () => {
        await fetch(`http://localhost:8090/payments/${payment.id}`, {
            method: 'DELETE'
        });
        li.remove();
    };

    li.appendChild(deleteButton);
    paymentList.appendChild(li);
}

// Fetch payments on page load
fetchPayments();