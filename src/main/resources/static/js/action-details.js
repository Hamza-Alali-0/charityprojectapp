document.addEventListener('DOMContentLoaded', function() {
    // Image gallery functionality
    const currentImage = document.getElementById('current-image');
    const thumbnails = document.querySelectorAll('.thumbnail');
    const mediaUrls = [];

    // Collect all media URLs
    thumbnails.forEach(thumbnail => {
        const imgSrc = thumbnail.querySelector('img').src;
        mediaUrls.push(imgSrc);
    });

    // Like button functionality
    const likeButton = document.getElementById('like-button');
    if (likeButton) {
        likeButton.addEventListener('click', function() {
            const actionId = this.getAttribute('data-action-id');
            const isLiked = this.getAttribute('data-liked') === 'true';
            const likeIcon = this.querySelector('i');
            const likeCount = this.querySelector('span');

            if (isLiked) {
                // Unlike the action
                unlikeAction(actionId).then(response => {
                    if (response.success) {
                        likeIcon.classList.remove('liked');
                        this.setAttribute('data-liked', 'false');
                        const currentCount = parseInt(likeCount.textContent);
                        likeCount.textContent = (currentCount - 1) + ' likes';
                    }
                });
            } else {
                // Like the action
                likeAction(actionId).then(response => {
                    if (response.success) {
                        likeIcon.classList.add('liked');
                        this.setAttribute('data-liked', 'true');
                        const currentCount = parseInt(likeCount.textContent);
                        likeCount.textContent = (currentCount + 1) + ' likes';
                    }
                });
            }
        });
    }

    // Participate button functionality
    const participateButton = document.getElementById('participate-button');
    if (participateButton) {
        participateButton.addEventListener('click', function() {
            const actionId = this.getAttribute('data-action-id');
            const isParticipated = this.classList.contains('participated');

            if (!isParticipated) {
                participateInAction(actionId).then(response => {
                    if (response.success) {
                        this.classList.add('participated');
                        this.querySelector('span').textContent = 'Vous participez à cette action';

                        // Update participant count
                        const participantsCount = document.querySelector('.participants-count span');
                        if (participantsCount) {
                            const countText = participantsCount.textContent;
                            const currentCount = parseInt(countText);
                            participantsCount.textContent = (currentCount + 1) + ' personnes participent à cette action';
                        }

                        // Update the stat in the header
                        const participantsStat = document.querySelector('.action-interaction .action-stat:nth-child(2) span');
                        if (participantsStat) {
                            const statText = participantsStat.textContent;
                            const currentStat = parseInt(statText);
                            participantsStat.textContent = (currentStat + 1) + ' participants';
                        }
                    }
                });
            }
        });
    }

    // Donation modal
    const donateButton = document.getElementById('donate-button');
    const donationModal = document.getElementById('donationModal');
    const closeDonationModal = donationModal.querySelector('.close');

    donateButton.addEventListener('click', function() {
        donationModal.style.display = 'block';
    });

    closeDonationModal.addEventListener('click', function() {
        donationModal.style.display = 'none';
    });

    // Donation form submission
    const donationForm = document.getElementById('donationForm');
    donationForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const actionId = document.getElementById('actionId').value;
        const montant = document.getElementById('montant').value;
        const commentaire = document.getElementById('commentaire').value;

        makeDonation(actionId, montant, commentaire).then(response => {
            if (response.success) {
                // Update progress bar and stats
                updateProgressDisplay(response.newAmount, response.percentage);

                // Close modal and reset form
                donationModal.style.display = 'none';
                donationForm.reset();

                // Show success message
                alert('Merci pour votre don !');

                // Reload page to show updated donations list
                window.location.reload();
            }
        });
    });

    // All donations modal
    const showDonationsButton = document.getElementById('show-donations');
    const donationsListModal = document.getElementById('donationsListModal');
    const closeDonationsModal = donationsListModal.querySelector('.close');

    if (showDonationsButton) {
        showDonationsButton.addEventListener('click', function(e) {
            e.preventDefault();
            donationsListModal.style.display = 'block';
        });

        closeDonationsModal.addEventListener('click', function() {
            donationsListModal.style.display = 'none';
        });
    }

    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target === donationModal) {
            donationModal.style.display = 'none';
        }
        if (event.target === donationsListModal) {
            donationsListModal.style.display = 'none';
        }
    });
});

// Updated changeImage function
function changeImage(index) {
    // Convert index to a number since it comes as a string from the onclick handler
    index = parseInt(index);

    const currentImage = document.getElementById('current-image');
    const thumbnails = document.querySelectorAll('.thumbnail');

    if (thumbnails.length <= index || index < 0) {
        console.error('Invalid image index:', index);
        return;
    }

    const selectedThumbnail = thumbnails[index];
    const imgSrc = selectedThumbnail.querySelector('img').src;

    // Update main image
    currentImage.src = imgSrc;

    // Update active thumbnail
    thumbnails.forEach(thumb => thumb.classList.remove('active'));
    selectedThumbnail.classList.add('active');

    console.log('Changed image to index:', index);
}
// Function to update progress display after donation
function updateProgressDisplay(newAmount, percentage) {
    // Update current amount
    const currentAmountElements = document.querySelectorAll('.stat-item:nth-child(2) strong');
    currentAmountElements.forEach(el => {
        el.textContent = newAmount.toFixed(2) + ' MAD';
    });

    // Update remaining amount
    const objectifElement = document.querySelector('.stat-item:nth-child(1) strong');
    if (objectifElement) {
        const objectif = parseFloat(objectifElement.textContent);
        const remaining = Math.max(0, objectif - newAmount);

        const remainingElement = document.querySelector('.stat-item:nth-child(3) strong');
        if (remainingElement) {
            remainingElement.textContent = remaining.toFixed(2) + ' MAD';
        }
    }

    // Update progress bar
    const progressFill = document.querySelector('.progress-fill');
    if (progressFill) {
        progressFill.style.width = percentage + '%';
    }

    // Update percentage text
    const percentageText = document.querySelector('.progress-percentage');
    if (percentageText) {
        percentageText.textContent = percentage.toFixed(1) + '%';
    }
}
// API functions without CSRF
async function likeAction(actionId) {
    try {
        const response = await fetch(`/user/actions/like/${actionId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return await response.json();
    } catch (error) {
        console.error('Error liking action:', error);
        return { success: false };
    }
}

async function unlikeAction(actionId) {
    try {
        const response = await fetch(`/user/actions/unlike/${actionId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return await response.json();
    } catch (error) {
        console.error('Error unliking action:', error);
        return { success: false };
    }
}

async function participateInAction(actionId) {
    try {
        const response = await fetch(`/user/actions/participate/${actionId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
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
                'Content-Type': 'application/json'
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