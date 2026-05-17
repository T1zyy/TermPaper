import { Text } from "@mantine/core";

export type EmptyRequestsProps = {
  text?: string;
};

export default function EmptyRequests({ text = "Здесь появятся новые заявки" }: EmptyRequestsProps) {
  return (
    <div className="request-empty">
      <div className="request-empty-icon">◎</div>
      <div>
        <Text fw={600}>Пока пусто</Text>
        <Text size="sm" c="dimmed">
          {text}
        </Text>
      </div>
    </div>
  );
}
