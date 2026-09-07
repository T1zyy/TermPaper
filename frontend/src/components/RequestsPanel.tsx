import { useEffect, useState } from "react";
import { Button, Card, Group, Stack, Text, Title } from "@mantine/core";
import { useTranslation } from "react-i18next";
import {
  acceptRequest,
  blockUser,
  declineRequest,
  getIncomingRequests,
  getOutgoingRequests,
  ConversationRequest
} from "../api";
import EmptyRequests from "./EmptyRequests";

export type RequestsPanelProps = {
  syncTick?: number;
  onChanged?: () => void;
};

export default function RequestsPanel({ syncTick, onChanged }: RequestsPanelProps) {
  const { t } = useTranslation();
  const [incoming, setIncoming] = useState<ConversationRequest[]>([]);
  const [outgoing, setOutgoing] = useState<ConversationRequest[]>([]);
  const [statusFilter, setStatusFilter] = useState<"ALL" | ConversationRequest["status"]>("ALL");
  const [direction, setDirection] = useState<"incoming" | "outgoing">("incoming");

  const statusLabel = (status: ConversationRequest["status"]) =>
    t(
      status === "PENDING"
        ? "statusPending"
        : status === "ACCEPTED"
        ? "statusAccepted"
        : "statusDeclined"
    );

  const load = async () => {
    const [inc, out] = await Promise.all([getIncomingRequests(), getOutgoingRequests()]);
    setIncoming(inc);
    setOutgoing(out);
    onChanged?.();
  };

  useEffect(() => {
    load();
  }, []);

  useEffect(() => {
    if (syncTick) {
      load();
    }
  }, [syncTick]);

  useEffect(() => {
    const timer = window.setInterval(load, 5000);
    return () => window.clearInterval(timer);
  }, []);

  const currentItems = direction === "incoming" ? incoming : outgoing;
  const filteredItems = currentItems.filter((req) => statusFilter === "ALL" || req.status === statusFilter);
  const blockIncoming = async (userId: number) => {
    await blockUser(userId);
    setIncoming((prev) => prev.filter((req) => req.senderId !== userId));
    setOutgoing((prev) => prev.filter((req) => req.recipientId !== userId));
    await load();
  };
  const removeRequestLocally = (requestId: number) => {
    setIncoming((prev) => prev.filter((req) => req.id !== requestId));
    setOutgoing((prev) => prev.filter((req) => req.id !== requestId));
  };

  return (
    <div className="panel">
      <Stack gap="sm">
        <Group justify="space-between" align="center">
          <Group gap={8}>
            <Button
              variant="light"
              className={direction === "incoming" ? "toggle-button toggle-button--active" : "toggle-button"}
              onClick={() => setDirection("incoming")}
            >
              {t("incoming")} {incoming.length}
            </Button>
            <Button
              variant="light"
              className={direction === "outgoing" ? "toggle-button toggle-button--active" : "toggle-button"}
              onClick={() => setDirection("outgoing")}
            >
              {t("outgoing")} {outgoing.length}
            </Button>
          </Group>
          <Group gap={8}>
          {(["ALL", "PENDING", "ACCEPTED", "DECLINED"] as const).map((status) => (
            <Button
              key={status}
              size="xs"
              variant="light"
              className={statusFilter === status ? "toggle-button toggle-button--active" : "toggle-button"}
              onClick={() => setStatusFilter(status)}
            >
              {status === "ALL" ? t("all") : statusLabel(status)}
            </Button>
          ))}
          </Group>
        </Group>

        <div className="requests-header">
          <div>
            <Title order={4}>{direction === "incoming" ? t("incoming") : t("outgoing")}</Title>
            <Text className="helper-text">{filteredItems.length} заявок</Text>
          </div>
          <span className="status-pill">{direction === "incoming" ? "⤵" : "⤴"}</span>
        </div>

        <Stack gap="sm">
          {filteredItems.length === 0 && <EmptyRequests text={t("noApplications")} />}
          {filteredItems.map((req) => (
            <Card key={req.id} className="request-card" withBorder={false}>
              <div className="request-row">
                <div className="avatar avatar--sm avatar--photo">
                  <span className="avatar-initials">
                    {((direction === "incoming" ? req.senderName : req.recipientName)?.[0] || "U").toUpperCase()}
                  </span>
                </div>
                <div className="request-info">
                  <Text fw={700}>
                    {direction === "incoming"
                      ? req.senderName || `ID #${req.senderId}`
                      : req.recipientName || `ID #${req.recipientId}`}
                  </Text>
                  <Text size="sm" c="dimmed">
                    {direction === "incoming"
                      ? `${req.senderAge ? `${req.senderAge} лет` : "—"} • ${req.senderCity || "—"}`
                      : `${req.recipientAge ? `${req.recipientAge} лет` : "—"} • ${req.recipientCity || "—"}`}
                  </Text>
                </div>
                <span className={`request-status request-status--${req.status.toLowerCase()}`}>
                  {statusLabel(req.status)}
                </span>
              </div>
              {req.message && <Text mt={8}>{req.message}</Text>}
              <Group mt={8}>
                {direction === "incoming" && req.status === "PENDING" && (
                  <>
                    <Button
                      onClick={async () => {
                        await acceptRequest(req.id);
                        removeRequestLocally(req.id);
                        await load();
                      }}
                    >
                      {t("accept")}
                    </Button>
                    <Button
                      variant="outline"
                      onClick={async () => {
                        await declineRequest(req.id);
                        removeRequestLocally(req.id);
                        await load();
                      }}
                    >
                      {t("decline")}
                    </Button>
                  </>
                )}
                {direction === "incoming" && (
                  <Button
                    variant="outline"
                    onClick={() => blockIncoming(req.senderId)}
                  >
                    {t("block")}
                  </Button>
                )}
              </Group>
            </Card>
          ))}
        </Stack>
      </Stack>
    </div>
  );
}
