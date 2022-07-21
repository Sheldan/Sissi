import json

from dto import LegacyCredits

def load_all_credits():
    with open('settings.json') as warnings:
        lines = warnings.read()
        credit_obj = json.loads(lines)
        user_list = credit_obj['384734293238749']['MEMBER']['297910194841583616']
        all_credit_dtos = []
        for user in user_list:
            user_obj = user_list[user]
            dto = LegacyCredits()
            dto.credits = user_obj['balance']
            dto.user_id = user
            all_credit_dtos.append(dto)

        print(f'loaded {len(all_credit_dtos)} credit entries.')
    return all_credit_dtos

