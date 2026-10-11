import { useEffect, useRef, useState } from "react";
import { ApiError, findSpots, imageUrl, type ApiPlace } from "./api";

type IconProps = {
  className?: string;
};

type Status = "intro" | "locating" | "loading" | "results" | "error";

type Spot = {
  key: string;
  name: string;
  type: string;
  address: string;
  distance: string | null;
  walk: string | null;
  image: string | null;
  score: number;
  mapUrl: string;
  accent: string;
};

const ACCENTS = ["#d8ff69", "#b4e9ff", "#ffd5e5", "#ffd983"];

const LOADING_STAGES = [
  { after: 0, label: "Scanning parks, gardens and benches" },
  { after: 8, label: "Pulling satellite views of each spot" },
  { after: 25, label: "Measuring walking routes" },
  { after: 45, label: "AI is ranking the calmest patches" },
];

function humanize(value: string): string {
  const text = value.replace(/[_-]+/g, " ").trim();
  return text.charAt(0).toUpperCase() + text.slice(1);
}

function formatDistance(meters: number): string {
  return meters < 1000 ? `${Math.round(meters)} m` : `${(meters / 1000).toFixed(1)} km`;
}

function toSpot(place: ApiPlace, index: number): Spot {
  const tags = place.tags ?? {};
  const props = place.properties ?? {};
  const rawType = tags.leisure || tags.amenity || tags.landuse || props.osm_value || "quiet spot";
  const type = humanize(rawType);
  const name = props.name || tags.name || type;
  const address =
    [props.street, props.district, props.city].filter(Boolean).join(", ") ||
    `${place.placeLatitude.toFixed(5)}, ${place.placeLongitude.toFixed(5)}`;
  const route = place.routeResult;

  return {
    key: `${place.placeLatitude}_${place.placeLongitude}_${index}`,
    name,
    type,
    address,
    distance: route ? formatDistance(route.distanceMeters) : null,
    walk: route ? `${route.durationMinutes} min walk` : null,
    image: imageUrl(place.imageName),
    score: Math.round(place.possibility ?? 0),
    mapUrl:
      place.googleMapUrl ||
      `https://www.google.com/maps/search/?api=1&query=${place.placeLatitude},${place.placeLongitude}`,
    accent: ACCENTS[index % ACCENTS.length],
  };
}

function LeafIcon({ className = "" }: IconProps) {
  return (
    <svg className={className} viewBox="0 0 32 32" fill="none" aria-hidden="true">
      <path
        d="M26.7 4.7C17 5.2 8.2 8.2 7 16.9c-.7 5.2 3.1 8.4 7.5 7.5 8.2-1.6 11.2-10.2 12.2-19.7Z"
        fill="currentColor"
      />
      <path d="M5 27c4.4-7.2 9.6-11.7 16.6-16.3" stroke="#163e25" strokeWidth="2.2" strokeLinecap="round" />
    </svg>
  );
}

function PinIcon({ className = "" }: IconProps) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M20 10c0 5.7-8 12-8 12s-8-6.3-8-12a8 8 0 1 1 16 0Z" stroke="currentColor" strokeWidth="1.8" />
      <circle cx="12" cy="10" r="2.6" fill="currentColor" />
    </svg>
  );
}

function ArrowIcon({ className = "" }: IconProps) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M5 12h13m-5-5 5 5-5 5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function LocateIcon({ className = "" }: IconProps) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="12" r="7" stroke="currentColor" strokeWidth="1.8" />
      <circle cx="12" cy="12" r="2.6" fill="currentColor" />
      <path d="M12 2v3m0 14v3M2 12h3m14 0h3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
    </svg>
  );
}

function SpotImage({ spot }: { spot: Spot }) {
  const [failed, setFailed] = useState(false);

  if (!spot.image || failed) {
    return (
      <div className="flex h-full w-full items-center justify-center bg-[radial-gradient(circle_at_30%_30%,#d8ff69_0%,#8fcf7a_45%,#3f7d4a_100%)]">
        <LeafIcon className="size-16 text-[#173f27]/40" />
      </div>
    );
  }

  return (
    <img
      src={spot.image}
      alt={`Satellite view of ${spot.name}`}
      loading="lazy"
      onError={() => setFailed(true)}
      className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-[1.04]"
    />
  );
}

export default function App() {
  const [status, setStatus] = useState<Status>("intro");
  const [spots, setSpots] = useState<Spot[]>([]);
  const [error, setError] = useState("");
  const [elapsed, setElapsed] = useState(0);
  const abortRef = useRef<AbortController | null>(null);
  const searchIdRef = useRef(0);

  const busy = status === "locating" || status === "loading";

  useEffect(() => {
    if (status !== "loading") return;
    setElapsed(0);
    const started = Date.now();
    const timer = window.setInterval(() => setElapsed(Math.floor((Date.now() - started) / 1000)), 1000);
    return () => window.clearInterval(timer);
  }, [status]);

  useEffect(() => () => abortRef.current?.abort(), []);

  const fail = (message: string) => {
    setError(message);
    setStatus("error");
  };

  const loadSpots = async (lat: number, lon: number) => {
    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;
    setStatus("loading");

    try {
      const data = await findSpots(lat, lon, controller.signal);
      setSpots((data.places ?? []).map(toSpot));
      setStatus("results");
    } catch (err) {
      if (err instanceof DOMException && err.name === "AbortError") return;
      if (err instanceof ApiError && err.status === 404) {
        setSpots([]);
        setStatus("results");
        return;
      }
      fail(err instanceof Error ? err.message : "Something went wrong. Please try again.");
    }
  };

  const findPlaces = () => {
    setError("");
    if (!navigator.geolocation) {
      fail("Location isn't available in this browser.");
      return;
    }
    setStatus("locating");
    const searchId = ++searchIdRef.current;
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        if (searchId === searchIdRef.current) loadSpots(coords.latitude, coords.longitude);
      },
      (geoError) => {
        if (searchId !== searchIdRef.current) return;
        fail(
          geoError.code === geoError.PERMISSION_DENIED
            ? "Location access was blocked. Allow it in your browser settings and try again."
            : "Couldn't get your location. Please try again.",
        );
      },
      { enableHighAccuracy: true, timeout: 15000 },
    );
  };

  const cancel = () => {
    searchIdRef.current++;
    abortRef.current?.abort();
    setStatus("intro");
  };

  const activeStage = LOADING_STAGES.reduce((acc, stage, i) => (elapsed >= stage.after ? i : acc), 0);

  return (
    <main className="min-h-screen overflow-hidden">
      <header className="safe-header relative z-20 mx-auto flex max-w-[1440px] items-center justify-between px-5 pb-5 sm:px-9 lg:px-14">
        <button
          id="home-button"
          className="group flex items-center gap-2.5 text-left"
          onClick={cancel}
          aria-label="Return home"
        >
          <span className="grid size-10 rotate-[-7deg] place-items-center rounded-[14px] bg-[#173f27] text-[#c9ff62] transition-transform group-hover:rotate-0">
            <LeafIcon className="size-6" />
          </span>
          <span className="font-display text-[18px] font-bold leading-none tracking-[-0.05em] text-[#173f27] min-[390px]:text-[20px] sm:text-[22px]">
            quite spot <span className="text-[#ef6938]">finder</span>
          </span>
        </button>
        <div className="flex items-center gap-2 rounded-full border border-[#173f27]/15 bg-white/55 px-3.5 py-2 text-xs font-semibold text-[#31543a] backdrop-blur">
          <span
            className={`size-2 rounded-full shadow-[0_0_0_4px_rgba(86,167,94,.13)] ${busy ? "animate-pulse bg-[#ef6938]" : "bg-[#56a75e]"}`}
          />
          <span className="hidden sm:inline">{busy ? "searching outdoors" : "outdoors is loading"}</span>
          <span className="sm:hidden">{busy ? "searching" : "ready"}</span>
        </div>
      </header>

      {status !== "results" ? (
        <section className="relative mx-auto flex min-h-[calc(100vh-80px)] max-w-[1440px] items-center px-5 pb-20 sm:px-9 lg:px-14">
          <div className="absolute left-[6%] top-[8%] size-32 rounded-full border border-[#173f27]/10 sm:size-56" />
          <div className="absolute -right-20 bottom-8 size-64 rounded-full bg-[#c9ff62]/50 blur-2xl sm:size-96" />
          <svg className="absolute right-[5%] top-[8%] hidden h-48 w-64 text-[#ef6938] lg:block" viewBox="0 0 260 190" fill="none" aria-hidden="true">
            <path d="M12 146C60 37 121 24 245 19M44 177c36-69 91-102 175-98" stroke="currentColor" strokeWidth="2" strokeDasharray="7 9" />
            <circle cx="12" cy="146" r="6" fill="currentColor" />
          </svg>

          <div className="relative z-10 grid w-full items-center gap-12 lg:grid-cols-[1.12fr_.88fr]">
            <div className="max-w-3xl">
              <div className="mb-7 inline-flex -rotate-2 items-center gap-2 rounded-full bg-[#ffdf78] px-4 py-2 text-sm font-bold text-[#173f27] shadow-[3px_4px_0_#173f27]">
                <span className="size-2 rounded-full bg-[#ef6938]" />
                SCREEN TIME INTERVENTION
              </div>
              <h1 className="font-display text-[clamp(4.2rem,9.5vw,9.3rem)] font-black leading-[0.79] tracking-[-0.085em] text-[#173f27]">
                GO
                <br />
                <span className="relative inline-block text-[#4f9c55]">
                  TOUCH
                  <svg className="absolute -bottom-4 left-1 h-6 w-full text-[#ef6938]" viewBox="0 0 360 24" preserveAspectRatio="none" fill="none" aria-hidden="true">
                    <path d="M3 17c75-16 184 11 354-10" stroke="currentColor" strokeWidth="6" strokeLinecap="round" />
                  </svg>
                </span>
                <br />
                GRASS.
              </h1>
              <p className="mt-10 max-w-lg text-lg leading-relaxed text-[#526457] sm:text-xl">
                We find the quiet parks, soft fields, and slow little trails hiding near you. No performance. Just outside.
              </p>
            </div>

            <div className="relative mx-auto w-full max-w-md lg:mr-6">
              <div className="absolute -left-8 -top-10 hidden rounded-full bg-[#ef6938] px-4 py-2 text-xs font-black uppercase tracking-wider text-white shadow-[3px_3px_0_#173f27] sm:block">
                your exit plan
              </div>
              <div className="relative rotate-[1.2deg] rounded-[32px] border-2 border-[#173f27] bg-[#fffdf5] p-7 shadow-[10px_12px_0_#173f27] sm:p-9">
                <div className="mb-8 grid size-16 place-items-center rounded-[22px] bg-[#d8ff69] text-[#173f27]">
                  <PinIcon className={`size-8 ${busy ? "animate-bounce" : ""}`} />
                </div>

                {status === "loading" ? (
                  <>
                    <h2 className="font-display text-4xl font-bold leading-[.95] tracking-[-0.055em] text-[#173f27]">
                      Looking for your patch...
                    </h2>
                    <p className="mt-4 leading-relaxed text-[#647268]">
                      This takes a minute or two. Good time to find your shoes.
                    </p>
                    <ol className="mt-7 space-y-3" aria-live="polite">
                      {LOADING_STAGES.map((stage, i) => (
                        <li key={stage.label} className="flex items-center gap-3 text-sm font-semibold">
                          <span
                            className={`grid size-5 shrink-0 place-items-center rounded-full border-2 ${
                              i < activeStage
                                ? "border-[#4f9c55] bg-[#4f9c55]"
                                : i === activeStage
                                  ? "animate-spin border-[#ef6938]/30 border-t-[#ef6938]"
                                  : "border-[#173f27]/15"
                            }`}
                          />
                          <span className={i <= activeStage ? "text-[#173f27]" : "text-[#8a968d]"}>{stage.label}</span>
                        </li>
                      ))}
                    </ol>
                    <div className="mt-8 flex items-center justify-between">
                      <span className="font-mono text-sm font-bold text-[#8a968d]">
                        {Math.floor(elapsed / 60)}:{String(elapsed % 60).padStart(2, "0")}
                      </span>
                      <button
                        id="cancel-search-button"
                        onClick={cancel}
                        className="rounded-full border-2 border-[#173f27] px-4 py-2 text-sm font-bold text-[#173f27] transition hover:bg-[#ffd5e5]"
                      >
                        Cancel
                      </button>
                    </div>
                  </>
                ) : (
                  <>
                    <h2 className="font-display text-4xl font-bold leading-[.95] tracking-[-0.055em] text-[#173f27]">
                      First, where are your feet?
                    </h2>
                    <p className="mt-4 leading-relaxed text-[#647268]">
                      Share your location once. We only use it to find calm spots nearby — nothing weird.
                    </p>
                    {status === "error" && (
                      <p
                        id="error-message"
                        role="alert"
                        className="mt-5 rounded-2xl border-2 border-[#a64826]/30 bg-[#ffe6dc] px-4 py-3 text-sm font-semibold text-[#a64826]"
                      >
                        {error}
                      </p>
                    )}
                    <button
                      id="use-location-button"
                      onClick={findPlaces}
                      disabled={busy}
                      className="mt-8 flex w-full items-center justify-between rounded-2xl bg-[#ef6938] px-5 py-4 font-bold text-white shadow-[0_5px_0_#a63d1a] transition-all hover:-translate-y-0.5 hover:bg-[#f47545] active:translate-y-1 active:shadow-none disabled:cursor-wait"
                    >
                      <span>
                        {status === "locating" ? "Finding your feet..." : status === "error" ? "Try again" : "Use my location"}
                      </span>
                      {status === "locating" ? (
                        <span className="size-5 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                      ) : (
                        <LocateIcon className="size-5" />
                      )}
                    </button>
                    <p className="mt-4 text-center text-[11px] font-semibold uppercase tracking-[0.14em] text-[#8a968d]">
                      precise enough to wander
                    </p>
                  </>
                )}
              </div>
            </div>
          </div>
        </section>
      ) : (
        <section className="relative mx-auto max-w-[1440px] px-5 pb-24 pt-10 sm:px-9 lg:px-14">
          <div className="mb-12 flex flex-col justify-between gap-7 md:flex-row md:items-end">
            <div>
              <div className="mb-4 flex items-center gap-2 text-sm font-bold uppercase tracking-[0.12em] text-[#ef6938]">
                <PinIcon className="size-5" />
                Around your current location
              </div>
              <h1 className="font-display max-w-3xl text-5xl font-black leading-[0.9] tracking-[-0.065em] text-[#173f27] sm:text-7xl">
                Your nearby <span className="text-[#4f9c55]">patches.</span>
              </h1>
              <p className="mt-5 max-w-xl text-lg text-[#667369]">
                {spots.length > 0
                  ? `${spots.length} calm ${spots.length === 1 ? "spot" : "spots"}, ranked by how peaceful they look. Distances are walking routes.`
                  : "Nothing calm enough nearby right now. Try again from a different spot."}
              </p>
            </div>
            <button
              id="update-location-button"
              onClick={findPlaces}
              className="flex w-fit items-center gap-2 rounded-full border-2 border-[#173f27] bg-[#fffdf5] px-5 py-3 text-sm font-bold text-[#173f27] transition hover:-rotate-1 hover:bg-[#d8ff69]"
            >
              <LocateIcon className="size-4" />
              Update location
            </button>
          </div>

          <div className="grid gap-6 md:grid-cols-2">
            {spots.map((spot, index) => (
              <article
                key={spot.key}
                className="group overflow-hidden rounded-[28px] border-2 border-[#173f27] bg-[#fffdf5] shadow-[6px_7px_0_#173f27] transition-transform duration-300 hover:-translate-y-1"
              >
                <div className="relative aspect-[16/9] overflow-hidden bg-[#cfe0c8]">
                  <SpotImage spot={spot} />
                  <div className="absolute inset-0 bg-gradient-to-t from-[#173f27]/45 via-transparent to-transparent" />
                  <span
                    className="absolute left-4 top-4 rounded-full border border-[#173f27]/15 px-3 py-1.5 text-xs font-extrabold uppercase tracking-wide text-[#173f27]"
                    style={{ backgroundColor: spot.accent }}
                  >
                    {spot.type}
                  </span>
                  <span className="absolute right-4 top-4 rounded-full bg-[#173f27] px-3 py-1.5 text-xs font-black text-[#d8ff69]">
                    {spot.score}% calm
                  </span>
                  <div className="absolute bottom-4 left-4 flex items-center gap-2">
                    {spot.distance && (
                      <span className="rounded-full bg-white px-3 py-1.5 text-xs font-bold text-[#173f27]">{spot.distance}</span>
                    )}
                    {spot.walk && (
                      <span className="rounded-full bg-[#173f27] px-3 py-1.5 text-xs font-bold text-white">{spot.walk}</span>
                    )}
                  </div>
                </div>
                <div className="flex flex-col gap-5 p-5 sm:flex-row sm:items-center sm:justify-between sm:p-6">
                  <div className="min-w-0">
                    <span className="mb-1 block text-xs font-bold text-[#ef6938]">
                      PATCH {String(index + 1).padStart(2, "0")}
                    </span>
                    <h2 className="font-display truncate text-3xl font-bold tracking-[-0.05em] text-[#173f27]">{spot.name}</h2>
                    <p className="mt-1 text-sm text-[#6c796f]">{spot.address}</p>
                  </div>
                  <a
                    id={`open-map-${index + 1}`}
                    href={spot.mapUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="flex shrink-0 items-center justify-between gap-5 rounded-2xl bg-[#173f27] px-5 py-3.5 text-sm font-bold text-white transition hover:bg-[#ef6938] sm:justify-start"
                  >
                    Open map
                    <ArrowIcon className="size-5" />
                  </a>
                </div>
              </article>
            ))}
          </div>
        </section>
      )}

      <div className="fixed bottom-4 left-4 -z-10 h-24 w-24 rounded-[45%] bg-[#ffdf78]/45 blur-xl" />
    </main>
  );
}
