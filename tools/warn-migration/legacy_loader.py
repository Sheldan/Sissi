import json
from datetime import datetime

from dto import LegacyWarning


def load_all_warnings():
    with open('settings.json') as warnings:
        lines = warnings.read()
        warnings_obj = json.loads(lines)
        warned_users = warnings_obj['260']['MODLOGS']['297910194841583616']
        all_warning_dtos = []
        for user in warned_users:
            warnings = warned_users[user]['x']
            for warning in warnings:
                warning_dto = LegacyWarning()
                warning_dto.level = warning['level']
                warning_dto.author_id = warning['author']
                warning_dto.reason = warning['reason']
                if 'duration' in warning and warning['duration'] is not None:
                    warning_dto.duration = int(float(warning['duration']))
                if 'until' in warning:
                    warning_dto.until = warning['until']
                warning_dto.date = datetime.fromtimestamp(int(warning['time']))
                warning_dto.user_id = user
                if 'modlog_message' in warning:
                    warning_dto.mod_log_channel_id = warning['modlog_message']['channel_id']
                    warning_dto.mod_log_message_id = warning['modlog_message']['message_id']
                all_warning_dtos.append(warning_dto)
        print(f'loaded {len(all_warning_dtos)} warnings.')
    return all_warning_dtos

