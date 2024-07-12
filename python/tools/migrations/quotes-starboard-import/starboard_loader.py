from sqlalchemy.sql import text


def load_all_starboard_posts(conn):
    squery = text("""select distinct sp.id, sp.author_user_in_server_id, sp.source_channel_id, sp.server_id, sp.post_message_id, spr.reactor_user_in_server_id, sp.created
from starboard_post sp
         inner join starboard_post_reaction spr
                    on sp.id = spr.post_id
                        and spr.reactor_user_in_server_id = (
                            select reactor_user_in_server_id
                            from starboard_post_reaction spr2
                            where spr2.post_id = sp.id
                            order by created limit 1
                        )
where sp.ignored = false
and sp.post_message_id not in (select message_id from quote)
 """)
    rs = conn.execute(squery)
    found_posts = []
    for post in rs:
        found_posts.append({
            'post_id': post[0],
            'channel_id': post[2],
            'message_id': post[4],
            'adder_id': post[5],
            'author_id': post[1],
            'server_id': post[3],
            'created': post[6]
       })
    return found_posts
