export function atLeast(n: number, min: number): number {
    return n < min ? min : n
}

export function distanceBetweenInMeters(lat1: number, long1: number, lat2: number, long2: number): number {
    const R = 6371; // Radius of the earth in km
    const dLat = deg2rad(lat2 - lat1);  // deg2rad below
    const dLon = deg2rad(long2 - long1);
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c * 1000; // Distance in meters
}

export function deg2rad(deg: number): number {
    return deg * (Math.PI / 180)
}

export function isSafe(value: any): boolean {
    return value !== null && value !== undefined
}
