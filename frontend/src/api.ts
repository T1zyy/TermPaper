export type Interest = {
  code: string;
  nameRu: string;
  nameEn: string;
  cluster: string;
};

export type Goal = {
  code: string;
  nameRu: string;
  nameEn: string;
};

export type UserProfile = {
  id: number;
  email: string;
  firstName: string;
  lastName?: string | null;
  age: number;
  city: string;
  language: string;
  gender: "MALE" | "FEMALE" | "OTHER";
  about?: string | null;
  interests: string[];
  goals: string[];
};

export type PublicUser = {
  id: number;
  firstName: string;
  lastName?: string | null;
  age: number;
  city: string;
  language: string;
  gender: "MALE" | "FEMALE" | "OTHER";
  about?: string | null;
  interests: string[];
  goals: string[];
};

export type Recommendation = PublicUser & {
  score: number;
};

const jsonHeaders = {
  "Content-Type": "application/json"
};

async function api<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    credentials: "include",
    ...options
  });

  if (!response.ok) {
    const text = await response.text();
    let message = `Request failed: ${response.status}`;
    if (text) {
      try {
        const data = JSON.parse(text) as { error?: string; message?: string };
        message = data.error || data.message || message;
      } catch {
        message = text;
      }
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }
  return JSON.parse(text) as T;
}

export async function login(email: string, password: string) {
  await api<void>("/api/auth/login", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify({ email, password })
  });
}

export async function logout() {
  await api<void>("/api/auth/logout", { method: "POST" });
}

export async function register(payload: {
  email: string;
  password: string;
  firstName: string;
  lastName?: string;
  age: number;
  city: string;
  language: string;
  gender: "MALE" | "FEMALE" | "OTHER";
  about?: string;
  interestCodes: string[];
  goalCodes?: string[];
}) {
  return api<UserProfile>("/api/auth/register", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  });
}

export async function me() {
  return api<UserProfile>("/api/auth/me");
}

export async function updateMe(payload: {
  firstName: string;
  lastName?: string;
  age: number;
  city: string;
  language: string;
  gender: "MALE" | "FEMALE" | "OTHER";
  about?: string;
  interestCodes: string[];
  goalCodes?: string[];
}) {
  return api<UserProfile>("/api/auth/me", {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  });
}

export async function getInterests() {
  return api<Interest[]>("/api/interests");
}

export async function getGoals() {
  return api<Goal[]>("/api/goals");
}

export async function getRecommendations(params: {
  ageFrom?: number;
  ageTo?: number;
  city?: string;
  language?: string;
  gender?: "MALE" | "FEMALE" | "OTHER";
  goals?: string[];
  interests?: string[];
  commonOnly?: boolean;
  q?: string;
}) {
  const search = new URLSearchParams();
  if (params.ageFrom) search.set("ageFrom", String(params.ageFrom));
  if (params.ageTo) search.set("ageTo", String(params.ageTo));
  if (params.city) search.set("city", params.city);
  if (params.language) search.set("language", params.language);
  if (params.gender) search.set("gender", params.gender);
  if (params.goals) params.goals.forEach((g) => search.append("goals", g));
  if (params.interests) params.interests.forEach((i) => search.append("interests", i));
  if (params.commonOnly) search.set("commonOnly", "true");
  if (params.q) search.set("q", params.q);
  const query = search.toString();
  const url = query ? `/api/recommendations?${query}` : "/api/recommendations";
  return api<Recommendation[]>(url);
}

export async function recordView(userId: number) {
  return api<void>(`/api/recommendations/${userId}/view`, { method: "POST" });
}

export async function sendRequest(recipientId: number, message?: string) {
  return api<void>("/api/requests", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify({ recipientId, message })
  });
}

export type ConversationRequest = {
  id: number;
  senderId: number;
  recipientId: number;
  senderName?: string | null;
  recipientName?: string | null;
  senderAge?: number | null;
  recipientAge?: number | null;
  senderCity?: string | null;
  recipientCity?: string | null;
  message?: string | null;
  status: "PENDING" | "ACCEPTED" | "DECLINED";
  createdAt: string;
  respondedAt?: string | null;
};

export type Chat = {
  id: number;
  companionId: number;
  companionName: string;
  companionAge: number;
  companionCity: string;
  lastMessage?: string | null;
  lastMessageAt: string;
};

export type ChatMessage = {
  id: number;
  conversationId: number;
  senderId: number;
  senderName: string;
  content: string;
  createdAt: string;
};

export async function getIncomingRequests() {
  return api<ConversationRequest[]>("/api/requests/incoming");
}

export async function getOutgoingRequests() {
  return api<ConversationRequest[]>("/api/requests/outgoing");
}

export async function acceptRequest(id: number) {
  return api<ConversationRequest>(`/api/requests/${id}/accept`, { method: "POST" });
}

export async function declineRequest(id: number) {
  return api<ConversationRequest>(`/api/requests/${id}/decline`, { method: "POST" });
}

export async function blockUser(userId: number) {
  return api<void>(`/api/blocks/${userId}`, { method: "POST" });
}

export async function postponeUser(userId: number) {
  return api<void>(`/api/postponed/${userId}`, { method: "POST" });
}

export async function removePostponedUser(userId: number) {
  return api<void>(`/api/postponed/${userId}`, { method: "DELETE" });
}

export async function getPostponedUsers() {
  return api<PublicUser[]>("/api/postponed");
}

export async function getChats() {
  return api<Chat[]>("/api/chats");
}

export async function getChatMessages(chatId: number) {
  return api<ChatMessage[]>(`/api/chats/${chatId}/messages`);
}
