package com.example.firebasechatapp.models

import com.example.firebasechatapp.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.received_messages_row.view.*
import kotlinx.android.synthetic.main.sent_messages_row.view.*

class ReceivedChatItem(val text : String) : Item<GroupieViewHolder>(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.receivedMessageText.text = text

    }

    override fun getLayout(): Int {
        return R.layout.received_messages_row
    }

}

class SentChatItem(val text : String) : Item<GroupieViewHolder>(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.sentMessageText.text = text

    }

    override fun getLayout(): Int {
        return R.layout.sent_messages_row
    }

}