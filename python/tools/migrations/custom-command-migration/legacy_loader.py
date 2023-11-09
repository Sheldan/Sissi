import json

from dto import LegacyCommand


def load_all_commands():
    with open('settings.json') as warnings:
        lines = warnings.read()
        command_obj = json.loads(lines)
        custom_commands = command_obj['414589031223512']['GUILD']['297910194841583616']['commands']
        all_command_dtos = []
        for command in custom_commands:
            custom_command = custom_commands[command]
            if custom_command is not None and 'response' in custom_command:
                if len(custom_command['response']) > 2048 or isinstance(custom_command['response'], list):
                    continue
                command_dto = LegacyCommand()
                command_dto.command = command
                command_dto.author_id = custom_command['author']['id']
                command_dto.response = custom_command['response']
                all_command_dtos.append(command_dto)
        print(f'loaded {len(all_command_dtos)} commands.')
    return all_command_dtos

