import firebase_admin
from firebase_admin import credentials, firestore
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import datetime
from flask import Flask, request

app = Flask(__name__)

# Initialize Firebase Admin SDK
cred = credentials.Certificate('eventure-96c2f-firebase-adminsdk-whed6-37c1dd9120.json')  # Path to your Firebase service account JSON file
firebase_admin.initialize_app(cred)

# Initialize Firestore
db = firestore.client()

# Gmail credentials
GMAIL_EMAIL = "mercy.ngaya@strathmore.edu" 
GMAIL_PASSWORD = "ofzb vrvv rhxv fgit "  # Replace with your actual Gmail app password

# Function to send email reminders
def send_email(to_email, subject, body):
    try:
        server = smtplib.SMTP('smtp.gmail.com', 587)
        server.starttls()
        server.login(GMAIL_EMAIL, GMAIL_PASSWORD)

        msg = MIMEMultipart()
        msg['From'] = GMAIL_EMAIL
        msg['To'] = to_email
        msg['Subject'] = subject
        msg.attach(MIMEText(body, 'plain'))

        server.sendmail(GMAIL_EMAIL, to_email, msg.as_string())
        server.quit()
        print(f"Email sent to {to_email}")
    except Exception as e:
        print(f"Failed to send email to {to_email}: {e}")

# Cloud Function to send reminders for events happening in one week
@app.route('/send_event_reminders', methods=['POST'])
def send_event_reminders(request=None):  # Accept the request argument
    try:
        now = datetime.datetime.now()
        next_week = now + datetime.timedelta(days=7)
        formatted_next_week = next_week.strftime("%d/%m/%Y")
        print(f"Looking for reminders with date: {formatted_next_week}")

        registrations_ref = db.collection('event_registrations')
        query = registrations_ref.where('eventDate', '==', formatted_next_week).where('reminderSent', '==', False)
        registrations = list(query.stream())

        if not registrations:
            print("No reminders to send today.")
            return 'No reminders to send today.', 200

        for doc in registrations:
            data = doc.to_dict()
            to_email = data.get('email')
            subject = f"Reminder: Upcoming Event - {data.get('eventName')}"
            body = (
                f"Hi {data.get('name')},\n\n"
                f"This is a friendly reminder about your upcoming event:\n\n"
                f"Event: {data.get('eventName')}\n"
                f"Date: {data.get('eventDate')}\n"
                f"Location: {data.get('eventLocation')}\n\n"
                "Thank you for registering. We look forward to seeing you there!\n\n"
                "Best regards,\nThe Event Team"
            )

            send_email(to_email, subject, body)
            doc.reference.update({'reminderSent': True})

        print("All reminders sent successfully!")
        return 'All reminders sent successfully!', 200
    
    except Exception as e:
        print(f"Error sending reminders: {e}")
        return f"Error sending reminders: {e}", 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
