export type RouteResult = {
  distanceMeters: number;
  durationMinutes: number;
};

export type ApiPlace = {
  placeLatitude: number;
  placeLongitude: number;
  tags: Record<string, string> | null;
  properties: Record<string, string> | null;
  imageName: string | null;
  routeResult: RouteResult | null;
  possibility: number;
  googleMapUrl: string | null;
};

export type SpotAddressesDetails = {
  currentLatitude: number;
  currentLongitude: number;
  places: ApiPlace[] | null;
};

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

const API_BASE = (import.meta.env.VITE_API_BASE_URL ?? "").replace(/\/$/, "");

export function imageUrl(imageName: string | null): string | null {
  if (!imageName) return null;
  const file = imageName.endsWith(".png") ? imageName : `${imageName}.png`;
  return `${API_BASE}/api/images/${encodeURIComponent(file)}`;
}

export async function findSpots(lat: number, lon: number, signal?: AbortSignal): Promise<SpotAddressesDetails> {
  const params = new URLSearchParams({ lat: String(lat), lon: String(lon) });
  let response: Response;
  try {
    response = await fetch(`${API_BASE}/api/ai/find-spots?${params}`, {
      headers: { Accept: "application/json" },
      signal,
    });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") throw error;
    throw new ApiError("Can't reach the spot finder server. Is the backend running on port 8080?", 0);
  }

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    try {
      const body = await response.json();
      if (body?.detail) message = body.detail;
    } catch {
      message = response.status === 502 || response.status === 504
        ? "Can't reach the spot finder server. Is the backend running on port 8080?"
        : message;
    }
    throw new ApiError(message, response.status);
  }

  return response.json();
}
