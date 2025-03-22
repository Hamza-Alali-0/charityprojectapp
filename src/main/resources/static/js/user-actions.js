document.addEventListener('DOMContentLoaded', function() {
    // Like button functionality
    const likeButtons = document.querySelectorAll('.like-btn');
    likeButtons.forEach(button => {
        button.addEventListener('click', function() {
            const actionId = this.getAttribute('data-action-id');
            const isLiked = this.getAttribute('data-liked') === 'true';
            const likeIcon = this.querySelector('i');
            const likeCount = this.querySelector('.like-count');

            // Toggle like state
            if (isLiked) {
                // Unlike the action
                unlikeAction(actionId).then(response => {
                    if (response.success) {
                        likeIcon.classList.remove('liked');
                        this.setAttribute('data-liked', 'false');
                        likeCount.textContent = parseInt(likeCount.textContent) - 1;
                    }
                });
            } else {
                // Like the action
                likeAction(actionId).then(response => {
                    if (response.success) {
                        likeIcon.classList.add('liked');
                        this.setAttribute('data-liked', 'true');
                        likeCount.textContent = parseInt(likeCount.textContent) + 1;
                    }
                });
            }
        });
    });

    // Participate button functionality
    const participateButtons = document.querySelectorAll('.participate-btn');
    participateButtons.forEach(button => {
        button.addEventListener('click', function() {
            const actionId = this.getAttribute('data-action-id');
            const isParticipated = this.classList.contains('participated');

            if (!isParticipated) {
                participateInAction(actionId).then(response => {
                    if (response.success) {
                        this.classList.add('participated');
                        this.querySelector('span').textContent = 'Participant';

                        // Update participant count
                        const participantsElement = document.querySelector(`.action-card[data-action-id="${actionId}"] .action-details .detail:nth-child(3) span`);
                        if (participantsElement) {
                            participantsElement.textContent = parseInt(participantsElement.textContent) + 1;
                        }
                    }
                });
            }
        });
    });

    // Donation button and modal
    const donateButtons = document.querySelectorAll('.donate-btn');
    const donationModal = document.getElementById('donationModal');
    const closeModalBtn = document.querySelector('.close');
    const actionIdInput = document.getElementById('actionId');

    donateButtons.forEach(button => {
        button.addEventListener('click', function() {
            const actionId = this.getAttribute('data-action-id');
            actionIdInput.value = actionId;
            donationModal.style.display = 'block';
        });
    });

    closeModalBtn.addEventListener('click', function() {
        donationModal.style.display = 'none';
    });

    window.addEventListener('click', function(event) {
        if (event.target === donationModal) {
            donationModal.style.display = 'none';
        }
    });

    // Donation form submission
    const donationForm = document.getElementById('donationForm');
    donationForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const actionId = actionIdInput.value;
        const montant = document.getElementById('montant').value;
        const commentaire = document.getElementById('commentaire').value;

        makeDonation(actionId, montant, commentaire).then(response => {
            if (response.success) {
                // Update the UI to reflect the donation
                const actionCard = document.querySelector(`.action-card[data-action-id="${actionId}"]`);
                if (actionCard) {
                    const progressFill = actionCard.querySelector('.progress-fill');
                    const currentAmount = actionCard.querySelector('.progress-text span:first-child');
                    const targetAmount = actionCard.querySelector('.progress-text span:last-child');

                    // Update current amount
                    const newAmount = parseFloat(currentAmount.textContent) + parseFloat(montant);
                    currentAmount.textContent = newAmount.toFixed(2) + ' MAD';

                    // Update progress bar
                    const target = parseFloat(targetAmount.textContent);
                    const percentage = Math.min((newAmount / target) * 100, 100);
                    progressFill.style.width = percentage + '%';
                }

                // Close modal and reset form
                donationModal.style.display = 'none';
                donationForm.reset();

                // Show success message
                alert('Merci pour votre don !');
            }
        });
    });
});

// API functions
// Update the likeAction function
async function likeAction(actionId) {
    try {
        const headers = {
            'Content-Type': 'application/json'
        };

        // Safely get CSRF token if available
        const csrfToken = document.querySelector('meta[name="_csrf"]');
        if (csrfToken) {
            headers['X-CSRF-TOKEN'] = csrfToken.content;
        }

        const response = await fetch(`/user/actions/like/${actionId}`, {
            method: 'POST',
            headers: headers
        });
        return await response.json();
    } catch (error) {
        console.error('Error liking action:', error);
        return { success: false };
    }
}

// Apply the same change to the other API functions (unlikeAction, participateInAction, makeDonation)
// Example for unlikeAction:
async function unlikeAction(actionId) {
    try {
        const headers = {
            'Content-Type': 'application/json'
        };

        // Safely get CSRF token if available
        const csrfToken = document.querySelector('meta[name="_csrf"]');
        if (csrfToken) {
            headers['X-CSRF-TOKEN'] = csrfToken.content;
        }

        const response = await fetch(`/user/actions/unlike/${actionId}`, {
            method: 'POST',
            headers: headers
        });
        return await response.json();
    } catch (error) {
        console.error('Error unliking action:', error);
        return { success: false };
    }
}

// Make similar changes to participateInAction and makeDonation functions
async function participateInAction(actionId) {
    try {
        const response = await fetch(`/user/actions/participate/${actionId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        return await response.json();
    } catch (error) {
        console.error('Error participating in action:', error);
        return { success: false };
    }
}

async function makeDonation(actionId, montant, commentaire) {
    try {
        const response = await fetch(`/user/actions/donate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            },
            body: JSON.stringify({
                actionId: actionId,
                montant: montant,
                commentaire: commentaire
            })
        });
        return await response.json();
    } catch (error) {
        console.error('Error making donation:', error);
        return { success: false };
    }
}