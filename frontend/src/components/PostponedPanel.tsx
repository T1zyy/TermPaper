import { useEffect, useState } from "react";
import { Button, Card, Group, Stack, Text, Textarea, TextInput, Title } from "@mantine/core";
import { useTranslation } from "react-i18next";
import {
  getPostponedUsers,
  PublicUser,
  removePostponedUser,
  sendRequest,
  UserProfile
} from "../api";
import EmptyRequests from "./EmptyRequests";
import ToastNotice from "./ToastNotice";

export type PostponedPanelProps = {
  user: UserProfile;
  interestOptions: { value: string; label: string }[];
  goalOptions: { value: string; label: string }[];
  syncTick?: number;
  onChanged?: () => void;
  onPostponedRemoved?: () => void;
};

export default function PostponedPanel({
  user,
  interestOptions,
  goalOptions,
  syncTick,
  onChanged,
  onPostponedRemoved
}: PostponedPanelProps) {
  const { t } = useTranslation();
  const [items, setItems] = useState<PublicUser[]>([]);
  const [messages, setMessages] = useState<Record<number, string>>({});
  const [toast, setToast] = useState<string | null>(null);
  const [query, setQuery] = useState("");

  const load = async () => {
    const data = await getPostponedUsers();
    setItems(data);
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

  const labelFor = (options: { value: string; label: string }[], value: string) =>
    options.find((item) => item.value === value)?.label || value;
  const filteredItems = items.filter((item) => {
    const text = [
      item.firstName,
      item.lastName || "",
      item.city,
      item.about || "",
      ...item.interests.map((interest) => labelFor(interestOptions, interest)),
      ...item.goals.map((goal) => labelFor(goalOptions, goal))
    ].join(" ").toLowerCase();
    return text.includes(query.trim().toLowerCase());
  });

  return (
    <div className="panel">
      <ToastNotice message={toast} onClose={() => setToast(null)} />
      <Group justify="space-between" align="center" mb={16}>
        <div>
          <Title order={4}>{t("postponedTab")}</Title>
          <Text className="helper-text">{items.length} {t("profiles")}</Text>
        </div>
      </Group>

      <Stack gap="sm">
        <TextInput
          label={t("search")}
          value={query}
          onChange={(event) => setQuery(event.currentTarget.value)}
        />
        {filteredItems.length === 0 && <EmptyRequests text={t("noPostponed")} />}
        {filteredItems.map((item) => {
          const commonInterests = item.interests.filter((interest) =>
            user.interests.includes(interest)
          );
          return (
            <Card key={item.id} className="request-card" withBorder={false}>
              <div className="request-row">
                <div className="avatar avatar--sm avatar--photo">
                  <span className="avatar-initials">
                    {(item.firstName?.[0] || "U").toUpperCase()}
                  </span>
                </div>
                <div className="request-info">
                  <Text fw={700}>
                    {item.firstName} {item.lastName || ""}
                  </Text>
                  <Text size="sm" c="dimmed">
                    {item.age} лет • {item.city}
                  </Text>
                </div>
                <span className="request-status">
                  {commonInterests.length} {t("commonInterestsShort")}
                </span>
              </div>

              {item.about && <Text mt={10}>{item.about}</Text>}

              <Group gap={8} mt={12}>
                {commonInterests.map((interest) => (
                  <span key={interest} className="badge badge--match">
                    {labelFor(interestOptions, interest)}
                  </span>
                ))}
                {item.goals.map((goal) => (
                  <span key={goal} className="badge">
                    {labelFor(goalOptions, goal)}
                  </span>
                ))}
              </Group>

              <Textarea
                label={t("message")}
                minRows={1}
                mt={12}
                value={messages[item.id] || ""}
                onChange={(event) =>
                  setMessages((prev) => ({ ...prev, [item.id]: event.currentTarget.value }))
                }
              />

              <Group mt={10}>
                <Button
                  onClick={async () => {
                    await sendRequest(item.id, messages[item.id] || undefined);
                    await removePostponedUser(item.id);
                    setItems((prev) => prev.filter((current) => current.id !== item.id));
                    setToast(t("requestSent"));
                    onPostponedRemoved?.();
                    await load();
                    onChanged?.();
                  }}
                >
                  {t("sendApplication")}
                </Button>
                <Button
                  variant="outline"
                  onClick={async () => {
                    await removePostponedUser(item.id);
                    setItems((prev) => prev.filter((current) => current.id !== item.id));
                    setToast(t("returnedToRecommendations"));
                    onPostponedRemoved?.();
                    await load();
                    onChanged?.();
                  }}
                >
                  {t("returnToRecommendations")}
                </Button>
              </Group>
            </Card>
          );
        })}
      </Stack>
    </div>
  );
}
