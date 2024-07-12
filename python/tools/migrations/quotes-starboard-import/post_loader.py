import requests
import os
import json
import time

token = os.getenv('TOKEN')

image_extension = ["jpg", "jpeg", "png", "gif", "webp", "tiff", "svg", "apng"]

def enrich_posts(posts):
    for post in posts:
        print(f"Loading post {post['message_id']}")
        url = f"https://discord.com/api/v10/channels/{post['channel_id']}/messages/{post['message_id']}"
        message = requests.get(url, headers={'Authorization': token})
        time.sleep(0.5)
        if message.status_code == 200:
            message_obj = json.loads(message.content)
            post['content'] = message_obj['content']
            attachments = []
            attachment_objs = message_obj['attachments']
            if len(attachment_objs) > 0:
                for attachment in attachment_objs:
                    extension = attachment['filename'][attachment['filename'].rfind('.') + 1]
                    attachment = {
                        'url': attachment['proxy_url'],
                        'is_image': extension.lower() in image_extension
                    }
                    attachments.append(attachment)
            post['attachments'] = attachments
        else:
            print(f"{post['message_id']}: Didnt find post {url}: {message.status_code}")
    return posts
