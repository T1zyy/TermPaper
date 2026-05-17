import { useEffect, useLayoutEffect, useMemo, useRef, useState } from "react";
import { Button, Group, Stack, Text, TextInput, Textarea, Title } from "@mantine/core";
import { useTranslation } from "react-i18next";
import { blockUser, Chat, ChatMessage, getChatMessages, getChats, UserProfile } from "../api";
import EmptyRequests from "./EmptyRequests";

export type ChatsPanelProps = {
  user: UserProfile;
  syncTick?: number;
  onChanged?: () => void;
};

export default function ChatsPanel({ user, syncTick, onChanged }: ChatsPanelProps) {
  const { t } = useTranslation();
  const [chats, setChats] = useState<Chat[]>([]);
  const [activeChatId, setActiveChatId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [chatQuery, setChatQuery] = useState("");
  const [messageQuery, setMessageQuery] = useState("");
  const [draft, setDraft] = useState("");
  const [status, setStatus] = useState<string | null>(null);
  const [messagesLoadedTick, setMessagesLoadedTick] = useState(0);
  const socketRef = useRef<WebSocket | null>(null);
  const chatWindowRef = useRef<HTMLDivElement | null>(null);
  const chatBottomRef = useRef<HTMLDivElement | null>(null);

  const scrollChatToBottom = () => {
    const scroll = () => {
      if (chatWindowRef.current) {
        chatWindowRef.current.scrollTop = 0;
      }
    };
    scroll();
    window.requestAnimationFrame(scroll);
    window.setTimeout(scroll, 0);
    window.setTimeout(scroll, 60);
    window.setTimeout(scroll, 180);
    window.setTimeout(scroll, 400);
  };

  const loadChats = async () => {
    const data = await getChats();
    setChats(data);
    setActiveChatId((prev) => prev ?? data[0]?.id ?? null);
    onChanged?.();
  };

  useEffect(() => {
    loadChats();
  }, []);

  useEffect(() => {
    if (syncTick) {
      loadChats();
    }
  }, [syncTick]);

  useEffect(() => {
    const timer = window.setInterval(loadChats, 5000);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    if (!activeChatId) {
      setMessages([]);
      return;
    }

    let cancelled = false;
    getChatMessages(activeChatId).then((data) => {
      if (!cancelled) {
        setMessages(data);
        setMessagesLoadedTick(Date.now());
        scrollChatToBottom();
      }
    });

    socketRef.current?.close();
    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    const socket = new WebSocket(`${protocol}://${window.location.host}/ws/chats?chatId=${activeChatId}`);
    socketRef.current = socket;
    socket.onmessage = (event) => {
      const message = JSON.parse(event.data) as ChatMessage;
      setMessages((prev) => [...prev, message]);
      scrollChatToBottom();
      setChats((prev) =>
        prev
          .map((chat) =>
            chat.id === message.conversationId
              ? { ...chat, lastMessage: message.content, lastMessageAt: message.createdAt }
              : chat
          )
          .sort((a, b) => Date.parse(b.lastMessageAt) - Date.parse(a.lastMessageAt))
      );
      onChanged?.();
    };
    socket.onclose = () => {
      if (socketRef.current === socket) {
        socketRef.current = null;
      }
    };

    return () => {
      cancelled = true;
      socket.close();
    };
  }, [activeChatId]);

  const activeChat = chats.find((chat) => chat.id === activeChatId) || null;
  const filteredChats = chats.filter((chat) =>
    chat.companionName.toLowerCase().includes(chatQuery.trim().toLowerCase())
  );
  const filteredMessages = useMemo(() => {
    const query = messageQuery.trim().toLowerCase();
    if (!query) {
      return messages;
    }
    return messages.filter((message) => message.content.toLowerCase().includes(query));
  }, [messages, messageQuery]);
  const visibleMessages = useMemo(() => [...filteredMessages].reverse(), [filteredMessages]);

  useLayoutEffect(() => {
    scrollChatToBottom();
  }, [filteredMessages.length, activeChatId, messagesLoadedTick]);

  const send = () => {
    const content = draft.trim();
    if (!content || socketRef.current?.readyState !== WebSocket.OPEN) {
      return;
    }
    socketRef.current.send(JSON.stringify({ content }));
    setDraft("");
  };

  return (
    <div className="split split--chat">
      <div className="panel">
        <Title order={4} mb={12}>{t("chats")}</Title>
        <Stack gap="sm">
          <TextInput
            label={t("searchChats")}
            value={chatQuery}
            onChange={(event) => setChatQuery(event.currentTarget.value)}
          />
          {filteredChats.length === 0 && <EmptyRequests text={t("noChats")} />}
          {filteredChats.map((chat) => (
            <button
              key={chat.id}
              type="button"
              className={chat.id === activeChatId ? "chat-item chat-item--active" : "chat-item"}
              onClick={() => setActiveChatId(chat.id)}
            >
              <span className="chat-item__name">{chat.companionName}</span>
              <span className="chat-item__meta">
                {chat.companionAge} лет • {chat.companionCity}
              </span>
              <span className="chat-item__message">{chat.lastMessage || t("noMessagesYet")}</span>
            </button>
          ))}
        </Stack>
      </div>

      <div className="panel">
        <Group justify="space-between" align="center" mb={16}>
          <div>
            <Title order={4}>{activeChat?.companionName || t("chat")}</Title>
            {activeChat && <Text className="helper-text">{activeChat.companionCity}</Text>}
          </div>
          <Group gap={8}>
            {status && <span className="status-pill">{status}</span>}
            {activeChat && (
              <Button
                variant="outline"
                onClick={async () => {
                  await blockUser(activeChat.companionId);
                  setStatus(t("block"));
                  setChats((prev) => prev.filter((chat) => chat.id !== activeChat.id));
                  setActiveChatId(chats.find((chat) => chat.id !== activeChat.id)?.id ?? null);
                  await loadChats();
                  onChanged?.();
                }}
              >
                {t("block")}
              </Button>
            )}
          </Group>
        </Group>

        {!activeChat ? (
          <EmptyRequests text={t("noChats")} />
        ) : (
          <Stack gap="sm">
            <TextInput
              label={t("searchMessages")}
              value={messageQuery}
              onChange={(event) => setMessageQuery(event.currentTarget.value)}
            />
            <div className="chat-window" ref={chatWindowRef} key={activeChatId}>
              {filteredMessages.length === 0 && (
                <Text className="helper-text">{t("noMessagesFound")}</Text>
              )}
              {visibleMessages.map((message) => (
                <div
                  key={message.id}
                  className={message.senderId === user.id ? "message-bubble message-bubble--mine" : "message-bubble"}
                >
                  <Text size="sm" fw={700}>{message.senderName}</Text>
                  <Text>{message.content}</Text>
                  <Text size="xs" c="dimmed">
                    {new Date(message.createdAt).toLocaleString()}
                  </Text>
                </div>
              ))}
              <div ref={chatBottomRef} />
            </div>
            <Group align="flex-end">
              <Textarea
                className="chat-input"
                label={t("message")}
                minRows={1}
                value={draft}
                onChange={(event) => setDraft(event.currentTarget.value)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" && !event.shiftKey) {
                    event.preventDefault();
                    send();
                  }
                }}
              />
              <Button onClick={send}>{t("send")}</Button>
            </Group>
          </Stack>
        )}
      </div>
    </div>
  );
}
