package com.ebay.park.model;

import java.io.Serializable;
import java.util.List;

import com.ebay.park.model.ConversationModel.Message.Action;

public class ConversationModel implements Serializable {

	private static final long serialVersionUID = 2720011980331310170L;

	private long conversationId;
	private long itemId;
	private long userId;
	private String username;
	private double currentPriceProposedByBuyer;
	private double currentPriceProposedBySeller;
	private Message lastChat;
	private String buyerThumbnail;
	private String sellerThumbnail;
	private String itemPicture;
	private boolean newChats;
	private boolean hasOffer;
	private Status status;
	private List<Message> chats;

	public enum Status {
		OPEN, CANCELLED, ACCEPTED;
	}

	public Double getAgreedPrice() {
		for (Message m : chats) {
			if (m.getAction() == Action.ACCEPTED) {
				return m.getOfferedPrice();
			}
		}
		return null;
	}

	public long getLastMessageTime() {
		if (lastChat != null) {
			return lastChat.getPostTime();
		} else {
			return 0;
		}

	}

	public double getCurrentPriceProposedByBuyer() {
		return currentPriceProposedByBuyer;
	}

	public void setCurrentPriceProposedByBuyer(double currentPriceProposedByBuyer) {
		this.currentPriceProposedByBuyer = currentPriceProposedByBuyer;
	}

	public double getCurrentPriceProposedBySeller() {
		return currentPriceProposedBySeller;
	}

	public void setCurrentPriceProposedBySeller(double currentPriceProposedBySeller) {
		this.currentPriceProposedBySeller = currentPriceProposedBySeller;
	}

	public String getItemPicture() {
		return itemPicture;
	}

	public void setItemPicture(String itemPicture) {
		this.itemPicture = itemPicture;
	}

	public Message getLastChatComment() {
		return lastChat;
	}

	public void setLastChatComment(Message lastChatComment) {
		this.lastChat = lastChatComment;
	}

	public boolean isNewChats() {
		return newChats;
	}

	public void setNewChats(boolean newChats) {
		this.newChats = newChats;
	}

	public void setHasOffer(boolean hasOffer){
		this.hasOffer = hasOffer;
	}

	public boolean hasOffer(){
		return hasOffer;
	}

	public String getBuyerThumbnail() {
		return buyerThumbnail;
	}

	public String getSellerThumbnail() {
		return sellerThumbnail;
	}

	public void setBuyerThumbnail(String buyerThumbnail) {
		this.buyerThumbnail = buyerThumbnail;
	}

	public void setSellerThumbnail(String sellerThumbnail) {
		this.sellerThumbnail = sellerThumbnail;
	}

	public long getConversationId() {
		return conversationId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setConversationId(long conversationId) {
		this.conversationId = conversationId;
	}

	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public List<Message> getChats() {
		return chats;
	}

	public void setChats(List<Message> chats) {
		this.chats = chats;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (conversationId ^ (conversationId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConversationModel other = (ConversationModel) obj;
		if (conversationId != other.conversationId)
			return false;
		return true;
	}

	public static class Message implements Serializable {
		private static final long serialVersionUID = -7927751104017970770L;

		private Action action;
		private long chatId;
		private String comment;
		private String hint;
		private double offeredPrice;
		private long postTime;
		private String senderUsername;
		private String receiverUsername;

		public enum Action {
			CHAT, ACCEPTED, CANCELLED, OFFER, MARKED_AS_SOLD
		}

		public long getChatId() {
			return chatId;
		}

		public void setChatId(long chatId) {
			this.chatId = chatId;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String getHint() {
			return hint;
		}

		public void setHint(String hint) {
			this.hint = hint;
		}

		public long getPostTime() {
			return postTime;
		}

		public void setPostTime(long postTime) {
			this.postTime = postTime;
		}

		public double getOfferedPrice() {
			return offeredPrice;
		}

		public void setOfferedPrice(double offeredPrice) {
			this.offeredPrice = offeredPrice;
		}

		public String getReceiverUsername() {
			return receiverUsername;
		}

		public void setReceiverUsername(String receiverUsername) {
			this.receiverUsername = receiverUsername;
		}

		public String getSenderUsername() {
			return senderUsername;
		}

		public void setSenderUsername(String senderUsername) {
			this.senderUsername = senderUsername;
		}

		public Action getAction() {
			return action;
		}

		public void setAction(Action action) {
			this.action = action;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (chatId ^ (chatId >>> 32));
			result = prime * result + ((comment == null) ? 0 : comment.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Message other = (Message) obj;
			if (chatId != other.chatId)
				return false;
			if (comment == null) {
				if (other.comment != null)
					return false;
			} else if (!comment.equals(other.comment))
				return false;
			return true;
		}

	}
}
