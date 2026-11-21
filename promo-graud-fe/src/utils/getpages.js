export default function getPages(current, total) {
  if (total <= 11) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  const pages = [];

  const startCount = 3;
  const endCount = 3;
  const window = 2; // 5 số quanh current

  const middleStart = Math.max(1, current - window);
  const middleEnd = Math.min(total, current + window);

  // START trượt theo middle
  let startStart = Math.max(1, middleStart - startCount - 1);

  // START
  for (let i = startStart; i < startStart + startCount; i++) {
    if (i < middleStart) pages.push(i);
  }

  // ... giữa START và MIDDLE
  if (middleStart > startStart + startCount) {
    pages.push('...');
  }

  // MIDDLE
  for (let i = middleStart; i <= middleEnd; i++) {
    pages.push(i);
  }

  // ... giữa MIDDLE và END
  if (middleEnd < total - endCount) {
    pages.push('...');
  }

  // END
  for (let i = total - endCount + 1; i <= total; i++) {
    if (i > middleEnd) pages.push(i);
  }

  return pages;
}
